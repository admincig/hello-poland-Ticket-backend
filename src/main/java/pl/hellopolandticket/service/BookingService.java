package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.ticket.market.Status.INVALID;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofBooking;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketWithQrCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopoland.dto.booking.BookingDTO;
import pl.hellopoland.dto.booking.TicketOrderDTO;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class BookingService extends ServiceSuperclass {

  private final static String TICKET_QR_CODE_HEIGHT_PROPERTY = "ticket.qrCode.height";
  private final static String TICKET_QR_CODE_WIDTH_PROPERTY = "ticket.qrCode.width";

  @Inject
  private BookingDao bookingDao;

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private Event<BookingMarkedAsBoughtEvent> bookingMarkedAsBoughtEvent;

  @Inject
  private ExceptionFactory exceptionFactory;

  public BookingDTO createBooking(BookingDTO booking) {
    Booking bookingToPersist = Booking.builder().date(new Date()).customerName(booking.customerName)
        .customerEmail(booking.customerEmail).build();

    List<Ticket> tickets = bookTickets(booking.ticketBookings, bookingToPersist);

    bookingToPersist.setTickets(tickets);

    return ofBooking(bookingDao.persist(bookingToPersist));
  }

  public BookingDTO markBookingAsBought(String serialNumber) {
    Booking booking = bookingDao.findBySerialNumber(serialNumber);

    if (booking.getStatus() == BOOKED) {
      booking.makeBought();
      sendEmailWithTicketQrCodes(booking);
    } else if (booking.getStatus() == INVALID) {
      bookTickets(null, booking);

      return markBookingAsBought(booking.getSerialNumber());
    } else {
      throw exceptionFactory.notBookedException();
    }

    return ofBooking(booking);
  }

  private synchronized List<Ticket> bookTickets(Collection<TicketOrderDTO> ticketBookingDTOs,
      Booking booking) {
    if (isANewBooking(ticketBookingDTOs)) {
      return book(ticketBookingDTOs, booking);
    } else {
      return rebook(booking);
    }
  }

  private List<Ticket> book(Collection<TicketOrderDTO> ticketBookingDTOs, Booking booking) {
    List<Ticket> bookedTickets = new ArrayList<>();

    for (TicketOrderDTO ticketBookingDTO : ticketBookingDTOs) {
      TicketDefinition ticketDefinition = ticketDefinitionService
          .findTicketDefinitionWithTicketPool(ticketBookingDTO.ticketDefinitionId,
              ticketBookingDTO.ticketPoolDefinitionId, ticketBookingDTO.date);

      Date date = ticketDefinition.getTicketPool().getPredefinedDate()
          ? ticketDefinition.getTicketPool().getDate()
          : ticketBookingDTO.date;

      for (int i = 0; i < ticketBookingDTO.numberOfTickets; i++) {
        Ticket ticket =
            Ticket.builder().name(ticketDefinition.getName()).price(ticketDefinition.getPrice())
                .date(date).dateType(ticketDefinition.getTicketPool().getDateType()).status(BOOKED)
                .booking(booking).ticketDefinition(ticketDefinition).build();

        bookedTickets.add(ticket);
      }

      ticketDefinition.decreaseAvailableTicketsNumber(ticketBookingDTO.numberOfTickets.intValue());
    }

    return ticketDao.persist(bookedTickets);
  }

  private List<Ticket> rebook(Booking booking) {
    List<Ticket> tickets = booking.getTickets();

    for (Ticket ticket : tickets) {
      ticket.getTicketDefinition().decreaseAvailableTicketsNumber(1);
      ticket.setStatus(BOOKED);
    }
    booking.setStatus(BOOKED);

    return tickets;
  }

  private boolean isANewBooking(Collection<TicketOrderDTO> ticketBookingDTOs) {
    return ticketBookingDTOs != null;
  }

  private void sendEmailWithTicketQrCodes(Booking booking) {
    int qrCodeWidth =
        valueOf(applicationPropertyService.findByName(TICKET_QR_CODE_WIDTH_PROPERTY).propertyValue);
    int qrCodeHeight = valueOf(
        applicationPropertyService.findByName(TICKET_QR_CODE_HEIGHT_PROPERTY).propertyValue);

    bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
        .customerName(booking.getCustomerName()).customerEmail(booking.getCustomerEmail())
        .tickets(booking.getTickets().stream()
            .map(ticket -> ofTicketWithQrCode(ticket,
                ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
            .collect(toList()))
        .build());
  }
}
