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
import pl.hellopolandticket.dao.AvailableTicketNumberAssociationDao;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;

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
  private TicketPoolService ticketPoolService;

  @Inject
  private TicketPoolDefinitionService ticketPoolDefinitionService;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private AvailableTicketNumberAssociationDao atnaDao;

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

  public void makeInvalid(Booking expiredBooking) {
    expiredBooking.setStatus(INVALID);
    for (Ticket t : expiredBooking.getTickets()) {
      var tp = t.getTicketPool();
      var td = t.getTicketDefinition();
      AvailableTicketNumberAssociation association =
          atnaDao.findForTicketPoolAndTicketDefinition(tp, td);
      Integer availableTicketsNumber = association.getAvailableTicketsNumber();
      Integer poolAvailableTicketNumber = tp.getAvailableTicketsNumber();
      if (poolAvailableTicketNumber == -1 && availableTicketsNumber == -1) {
        // nothing to do
      } else if (poolAvailableTicketNumber == -1 && availableTicketsNumber > -1) {
        association.setAvailableTicketsNumber(availableTicketsNumber + 1);
        atnaDao.update(association);
      } else if (poolAvailableTicketNumber > -1 && availableTicketsNumber == -1) {
        tp.increaseAvailableTicketsNumber();
      } else if (poolAvailableTicketNumber > -1 && availableTicketsNumber > -1) {
        tp.increaseAvailableTicketsNumber();
        association.setAvailableTicketsNumber(availableTicketsNumber + 1);
        atnaDao.update(association);
      }
    }
  }

  private synchronized List<Ticket> bookTickets(Collection<TicketOrderDTO> ticketBookingDTOs,
      Booking booking) {
    if (isANewBooking(ticketBookingDTOs)) {
      return book(ticketBookingDTOs, booking);
    } else {
      return rebook(booking);
    }
  }

  private List<Ticket> book(Collection<TicketOrderDTO> dtos, Booking booking) {
    List<Ticket> bookedTickets = new ArrayList<>();
    for (TicketOrderDTO dto : dtos) {
      TicketDefinition ticketDefinition = ticketDefinitionService.get(dto.ticketDefinitionId);
      if (!ticketDefinition.isConnectedWithPoolDefiniton(dto.ticketPoolDefinitionId)) {
        throw new ResourceNotFoundException(
            "Ticket definition does not belong to given pool definition");
      }
      TicketPoolDefinition poolDefinition =
          ticketPoolDefinitionService.get(dto.ticketPoolDefinitionId);
      TicketPool pool = ticketPoolService.findOrCreateNew(poolDefinition,
          poolDefinition.getIsCyclic() ? dto.date : null);

      for (int i = 0; i < dto.numberOfTickets; i++) {
        Ticket ticket = Ticket.builder().name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice()).date(dto.date).status(BOOKED).booking(booking)
            .ticketDefinition(ticketDefinition).ticketPool(pool).build();

        bookedTickets.add(ticket);
      }
      checkAndDecreaseAvailability(pool, ticketDefinition, dto.numberOfTickets.intValue());
      // pool.decreaseAvailableTicketsNumber(dto.numberOfTickets.intValue());
    }
    return ticketDao.persist(bookedTickets);
  }

  private List<Ticket> rebook(Booking booking) {
    List<Ticket> tickets = booking.getTickets();
    for (Ticket ticket : tickets) {
      checkAndDecreaseAvailability(ticket.getTicketPool(), ticket.getTicketDefinition(), 1);
      // ticket.getTicketPool().decreaseAvailableTicketsNumber(1);
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

  private void checkAndDecreaseAvailability(TicketPool pool, TicketDefinition ticketDefinition,
      int numberOfTickets) {
    Integer poolAvailableTicketNumber = pool.getAvailableTicketsNumber();
    AvailableTicketNumberAssociation association =
        atnaDao.findForTicketPoolAndTicketDefinition(pool, ticketDefinition);
    Integer availableTicketsNumber = association.getAvailableTicketsNumber();
    if (poolAvailableTicketNumber == -1 && availableTicketsNumber == -1) {
      // nothing to do
      return;
    } else if (poolAvailableTicketNumber == -1 && availableTicketsNumber > 0) {
      var number = availableTicketsNumber - numberOfTickets;
      if (number >= 0) {
        association.setAvailableTicketsNumber(number);
        atnaDao.update(association);
      } else {
        throw new NoAvailableTicketsException();
      }
      return;
    } else if (poolAvailableTicketNumber > 0 && availableTicketsNumber == -1) {
      var number = poolAvailableTicketNumber - numberOfTickets;
      if (number >= 0) {
        pool.decreaseAvailableTicketsNumber(numberOfTickets);
      } else {
        throw new NoAvailableTicketsException();
      }
      return;
    } else if (poolAvailableTicketNumber > 0 && availableTicketsNumber > 0) {
      var poolNumber = poolAvailableTicketNumber - numberOfTickets;
      var availableNumber = availableTicketsNumber - numberOfTickets;
      if (poolNumber >= 0 && availableNumber >= 0) {
        pool.decreaseAvailableTicketsNumber(numberOfTickets);
        association.setAvailableTicketsNumber(availableNumber);
        atnaDao.update(association);
      } else {
        throw new NoAvailableTicketsException();
      }
      return;
    }
    throw new NoAvailableTicketsException();
  }

}
