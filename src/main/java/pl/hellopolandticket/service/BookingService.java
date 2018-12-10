package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.ticket.market.Status.INVALID;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofBooking;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketWithQrCode;
import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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

  private final Logger logger = System.getLogger(this.getClass().getName());

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
        .customerEmail(booking.customerEmail).partnersEmails(booking.partnersEmails).build();

    if (booking.sightEventPdfAttachments != null && !booking.sightEventPdfAttachments.isEmpty()) {
      bookingToPersist.setSightEventPdfAttachmentsPaths(booking.sightEventPdfAttachments.stream()
          .map(pdf -> pdf.path).collect(Collectors.toSet()));
    }
    logger.log(Logger.Level.INFO, "...........Start booking tickets..............");
    List<Ticket> tickets = bookTickets(booking.ticketBookings, bookingToPersist);
    bookingToPersist.setTickets(tickets);
    logger.log(Logger.Level.INFO, "...........End booking tickets..............");
    return ofBooking(bookingDao.persist(bookingToPersist));
  }

  public BookingDTO markBookingAsBought(String serialNumber, String p24OrderId,
      String p24Currency) {
    logger.log(Logger.Level.INFO, "...........Start buying tickets..............");
    Booking booking = bookingDao.findBySerialNumber(serialNumber);
    if (booking.getStatus() == BOOKED) {
      booking.makeBought(p24OrderId, p24Currency);
      sendEmailWithTicketQrCodes(booking);
    } else if (booking.getStatus() == INVALID) {
      bookTickets(null, booking);

      return markBookingAsBought(booking.getSerialNumber(), p24OrderId, p24Currency);
    } else {
      throw exceptionFactory.notBookedException();
    }
    logger.log(Logger.Level.INFO, "...........End buying tickets..............");
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
        logger.log(Logger.Level.ERROR,
            "ResourceNotFoundException: Ticket definition id=[" + ticketDefinition.getId()
                + "] does not belong to given pool definition id=[" + dto.ticketPoolDefinitionId
                + "]");
        throw new ResourceNotFoundException(
            "Ticket definition does not belong to given pool definition");
      }
      TicketPoolDefinition poolDefinition =
          ticketPoolDefinitionService.get(dto.ticketPoolDefinitionId);
      TicketPool pool = ticketPoolService.findOrCreateNew(poolDefinition, dto.date);
      for (int i = 0; i < dto.numberOfTickets; i++) {
        Ticket ticket = Ticket.builder().name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice()).date(dto.date).status(BOOKED).booking(booking)
            .ticketDefinition(ticketDefinition).ticketPool(pool).build();

        bookedTickets.add(ticket);
      }
      checkAndDecreaseAvailability(pool, ticketDefinition, dto.numberOfTickets.intValue());
    }
    return ticketDao.persist(bookedTickets);
  }

  private List<Ticket> rebook(Booking booking) {
    List<Ticket> tickets = booking.getTickets();
    for (Ticket ticket : tickets) {
      checkAndDecreaseAvailability(ticket.getTicketPool(), ticket.getTicketDefinition(), 1);
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

    if (booking.getSightEventPdfAttachmentsPaths() != null) {
      booking.getSightEventPdfAttachmentsPaths().size();
    }
    bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
        .p24Currency(booking.getP24Currency()).p24OrderId(booking.getP24OrderId())
        .customerName(booking.getCustomerName()).customerEmail(booking.getCustomerEmail())
        .tickets(booking.getTickets().stream()
            .map(ticket -> ofTicketWithQrCode(ticket,
                ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
            .collect(toList()))
        .sightEventPdfAttachmentsPaths(booking.getSightEventPdfAttachmentsPaths())
        .partnersEmails(booking.getPartnersEmails()).build());
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
        logger.log(Logger.Level.ERROR,
            "NoAvailableTicketsException: TicketPool id=[" + pool.getId()
                + "], TicketDefinition id=[" + ticketDefinition.getId() + "], numberOfTickets="
                + numberOfTickets);
        throw new NoAvailableTicketsException();
      }
      return;
    } else if (poolAvailableTicketNumber > 0 && availableTicketsNumber == -1) {
      var number = poolAvailableTicketNumber - numberOfTickets;
      if (number >= 0) {
        pool.decreaseAvailableTicketsNumber(numberOfTickets);
      } else {
        logger.log(Logger.Level.ERROR,
            "NoAvailableTicketsException: TicketPool id=[" + pool.getId()
                + "], TicketDefinition id=[" + ticketDefinition.getId() + "], numberOfTickets="
                + numberOfTickets);
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
        logger.log(Logger.Level.ERROR,
            "NoAvailableTicketsException: TicketPool id=[" + pool.getId()
                + "], TicketDefinition id=[" + ticketDefinition.getId() + "], numberOfTickets="
                + numberOfTickets);
        throw new NoAvailableTicketsException();
      }
      return;
    }
    logger.log(Logger.Level.ERROR,
        "NoAvailableTicketsException: TicketPool id=[" + pool.getId() + "], TicketDefinition id=["
            + ticketDefinition.getId() + "], numberOfTickets=" + numberOfTickets);
    throw new NoAvailableTicketsException();
  }

}
