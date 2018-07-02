package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.Status.BOOKED;
import static pl.hellopolandticket.model.Status.INVALID;
import static pl.hellopolandticket.service.dto.BookingDTO.ofBooking;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicketWithQrCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.dto.BookingDTO;
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
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private Event<BookingMarkedAsBoughtEvent> bookingMarkedAsBoughtEvent;

  @Inject
  private ExceptionFactory exceptionFactory;

  public BookingDTO createBooking(pl.hellopoland.dto.booking.Booking booking) {
    Booking bookingToPersist = Booking.builder()
        .date(new Date())
        .customerName(booking.customerName)
        .customerEmail(booking.customerEmail)
        .build();

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

  private synchronized List<Ticket> bookTickets(
      Collection<pl.hellopoland.dto.booking.Ticket> ticketBookingDTOS, Booking booking) {
    if (isANewBooking(ticketBookingDTOS)) {
      return book(ticketBookingDTOS, booking);
    } else {
      return rebook(booking);
    }
  }

  private List<Ticket> book(Collection<pl.hellopoland.dto.booking.Ticket> ticketBookingDTOS,
      Booking booking) {
    List<Ticket> bookedTickets = new ArrayList<>();

    for (pl.hellopoland.dto.booking.Ticket ticketBookingDTO : ticketBookingDTOS) {
      TicketDefinition ticketDefinition = ticketDefinitionDao
          .findById(ticketBookingDTO.ticketDefinitionId);

      SightEvent sightEvent = ticketDefinition.getSightEvent();

      Date date =
          ticketDefinition.getPredefinedDate() ? ticketDefinition.getDate() : ticketBookingDTO.date;

      for (int i = 0; i < ticketBookingDTO.numberOfTickets; i++) {
        Ticket ticket = Ticket.builder()
            .sightEvent(sightEvent)
            .name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice())
            .date(date)
            .dateType(ticketDefinition.getDateType())
            .status(BOOKED)
            .booking(booking)
            .ticketDefinition(ticketDefinition)
            .build();

        bookedTickets.add(ticket);
      }

      sightEvent.decreaseAvailableTicketsNumber(
          ticketBookingDTO.numberOfTickets.intValue());
    }

    return ticketDao.persist(bookedTickets);
  }

  private List<Ticket> rebook(Booking booking) {
    List<Ticket> tickets = booking.getTickets();

    for (Ticket ticket : tickets) {
      ticket.getSightEvent().decreaseAvailableTicketsNumber(1);
      ticket.setStatus(BOOKED);
    }
    booking.setStatus(BOOKED);

    return tickets;
  }

  private boolean isANewBooking(Collection<pl.hellopoland.dto.booking.Ticket> ticketBookingDTOs) {
    return ticketBookingDTOs != null;
  }

  private void sendEmailWithTicketQrCodes(Booking booking) {
    int qrCodeWidth = valueOf(
        applicationPropertyService.findByName(TICKET_QR_CODE_WIDTH_PROPERTY).getPropertyValue());
    int qrCodeHeight = valueOf(
        applicationPropertyService.findByName(TICKET_QR_CODE_HEIGHT_PROPERTY).getPropertyValue());

    bookingMarkedAsBoughtEvent.fireAsync(
        BookingMarkedAsBoughtEvent.builder()
            .customerName(booking.getCustomerName())
            .customerEmail(booking.getCustomerEmail())
            .tickets(booking.getTickets().stream()
                .map(ticket -> ofTicketWithQrCode(ticket,
                    ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
                .collect(toList()))
            .build());
  }
}