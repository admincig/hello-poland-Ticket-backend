package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.INVALID;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofBooking;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketWithQrCode;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import pl.hellopoland.dto.EmailSendingReportDTO;
import pl.hellopoland.dto.booking.BookingDTO;
import pl.hellopoland.dto.booking.TicketOrderDTO;
import pl.hellopolandticket.dao.AvailableTicketNumberAssociationDao;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.exception.badrequest.EmailSendingException;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;
import pl.hellopolandticket.service.util.EmailSendingReport;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

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
  private TicketPoolService ticketPoolService;
  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;
  @Inject
  private ApplicationPropertyService applicationPropertyService;
  @Inject
  private AvailableTicketNumberAssociationDao atnaDao;
  @Inject
  private Event<BookingMarkedAsBoughtEvent> bookingMarkedAsBoughtEvent;
  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private EmailSenderService emailService;
  @Inject
  private TicketService ticketService;
  @Inject
  private TicketPoolQuantityMonitoringService poolSizeMonitoringService;

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public BookingDTO createBooking(BookingDTO booking) {
    Booking bookingToPersist = Booking.builder()
        .date(new Date())
        .customerName(booking.customerName)
        .customerEmail(booking.customerEmail)
        .invoice(booking.invoice)
        .buyerNotes(booking.buyerNotes)
        .build();
    logger.log(Logger.Level.INFO, "...........Start booking tickets..............");
    List<Ticket> tickets = bookTickets(booking.ticketBookings, bookingToPersist);
    bookingToPersist.setTickets(tickets);
    var bo = bookingDao.persist(bookingToPersist);
    logger.log(Logger.Level.INFO, "Created booking id=" + bo.getId());
    logger.log(Logger.Level.INFO, "...........End booking tickets..............");
    return ofBooking(bo);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public BookingDTO markBookingAsBought(String serialNumber, String p24OrderId,
      String p24Currency) {
    logger.log(Logger.Level.INFO, "...........Start buying tickets..............");
    Booking booking = bookingDao.findBySerialNumber(serialNumber);
    if (booking.getStatus() == BOOKED) {
      makeBought(booking, p24OrderId, p24Currency);
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

  public void makeBought(Booking booking, String p24OrderId, String p24Currency) {
    booking.setStatus(BOUGHT);
    booking.setP24OrderId(p24OrderId);
    booking.setP24Currency(p24Currency);
    booking.getTickets().forEach(t -> ticketService.setStatusAsBought(t));

    poolSizeMonitoringService
        .informPartnerAboutTicketsNumberLeftToBuyRunningOut(booking.getTickets());
  }

  @RolesAllowed({ROLE_ADMIN})
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
      TicketDefinition ticketDefinition = ticketDefinitionDao.findById(dto.ticketDefinitionId);
      if (!ticketDefinition.isConnectedWithPoolDefiniton(dto.ticketPoolDefinitionId)) {
        logger.log(Logger.Level.ERROR,
            "ResourceNotFoundException: Ticket definition id=[" + ticketDefinition.getId()
                + "] does not belong to given pool definition id=[" + dto.ticketPoolDefinitionId
                + "]");
        throw new ResourceNotFoundException(
            "Ticket definition does not belong to given pool definition");
      }
      TicketPoolDefinition poolDefinition =
          ticketPoolDefinitionDao.findById(dto.ticketPoolDefinitionId);
      TicketPool pool = ticketPoolService.find(poolDefinition, dto.date);
      if (pool == null) {
        pool = ticketPoolService.createNew(poolDefinition, dto.date);
        em.refresh(ticketDefinition);
      }
      for (int i = 0; i < dto.numberOfTickets; i++) {
        AvailableTicketNumberAssociation atna = ticketDefinition.getAtna(pool);
        Ticket ticket = Ticket.builder().name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice()).date(pool.getStartDate()).status(BOOKED)
            .discount(atna.getDiscount())
            .booking(booking).ticketDefinition(ticketDefinition).ticketPool(pool).build();
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
    var allPdfs = getDistinctPdfsForTickets(booking.getTickets());

    // sending email to buyer:
    bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
        .p24Currency(booking.getP24Currency())
        .hash(booking.getSerialNumber())
        .customerName(booking.getCustomerName())
        .recipientEmail(booking.getCustomerEmail())
        .buyerNotes(booking.getBuyerNotes())
        .tickets(booking.getTickets().stream()
            .map(ticket -> ofTicketWithQrCode(ticket,
                ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
            .collect(toList()))
        .sightEventPdfAttachmentsPaths(allPdfs)
        .build()
    );

    // sending email to helpdesk:
    bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
        .p24Currency(booking.getP24Currency())
        .hash(booking.getSerialNumber())
        .customerName(booking.getCustomerName())
        .recipientEmail(applicationPropertyService.findByName("mail.ticket.copy").propertyValue)
        .buyerNotes(booking.getBuyerNotes())
        .invoice(booking.getInvoice())
        .tickets(booking.getTickets().stream()
            .map(ticket -> ofTicketWithQrCode(ticket,
                ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
            .collect(toList()))
        .sightEventPdfAttachmentsPaths(allPdfs)
        .build()
    );

    // sending email to partners:
    var ticketsByPartner = booking.getTickets().stream().collect(Collectors
        .groupingBy(t -> t.getTicketPool().getTicketPoolDefinition().getSightEvent().getPartner()));
    for (Entry<Partner, List<Ticket>> entry : ticketsByPartner.entrySet()) {
      bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
          .p24Currency(booking.getP24Currency())
          .hash(booking.getSerialNumber())
          .customerName(booking.getCustomerName())
          .recipientEmail(entry.getKey().getEmail())
          .buyerNotes(booking.getBuyerNotes())
          .invoice(booking.getInvoice())
          .tickets(entry.getValue().stream()
              .map(ticket -> ofTicketWithQrCode(ticket,
                  ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
              .collect(toList()))
          .sightEventPdfAttachmentsPaths(getDistinctPdfsForTickets(entry.getValue()))
          .replyToEmail(booking.getCustomerEmail())
          .build()
      );
    }
  }

  private Set<String> getDistinctPdfsForTickets(List<Ticket> tickets) {
    return tickets.stream().map(Ticket::getTicketPool).map(TicketPool::getTicketPoolDefinition)
        .map(TicketPoolDefinition::getSightEvent)
        .filter(se -> se.getPdfAttachmentsPaths() != null && !se.getPdfAttachmentsPaths().isEmpty())
        .map(SightEvent::getPdfAttachmentsPaths).flatMap(Collection::stream).distinct()
        .collect(Collectors.toSet());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_ADMIN})
  public EmailSendingReportDTO sendTicketCopy(String serialNumber) {
    var booking = bookingDao.findBySerialNumber(serialNumber);
    int qrCodeWidth =
        valueOf(applicationPropertyService.findByName(TICKET_QR_CODE_WIDTH_PROPERTY).propertyValue);
    int qrCodeHeight = valueOf(
        applicationPropertyService.findByName(TICKET_QR_CODE_HEIGHT_PROPERTY).propertyValue);
    List<Ticket> tickets = Collections.emptyList();
    User loggedUser = getLoggedUser();
    if (loggedUser.hasRole(ROLE_ADMIN)) {
      tickets = booking.getTickets();

      // sending email to partners (via an event asynchronously):
      var ticketsByPartner = tickets.stream().collect(Collectors.groupingBy(
          t -> t.getTicketPool().getTicketPoolDefinition().getSightEvent().getPartner()));
      for (Entry<Partner, List<Ticket>> entry : ticketsByPartner.entrySet()) {
        bookingMarkedAsBoughtEvent.fireAsync(BookingMarkedAsBoughtEvent.builder()
            .p24Currency(booking.getP24Currency())
            .hash(booking.getSerialNumber())
            .customerName(booking.getCustomerName())
            .buyerNotes(booking.getBuyerNotes())
            .recipientEmail(entry.getKey().getEmail())
            .tickets(entry.getValue().stream()
                .map(ticket -> ofTicketWithQrCode(ticket,
                    ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
                .collect(toList()))
            .sightEventPdfAttachmentsPaths(getDistinctPdfsForTickets(entry.getValue()))
            .replyToEmail(booking.getCustomerEmail())
            .build()
        );
      }

      // sending email to buyer and in bcc to helpdesk:
      try {
        EmailSendingReport report = emailService.sendEmailWithQrCodes(BookingMarkedAsBoughtEvent.builder()
            .p24Currency(booking.getP24Currency())
            .hash(booking.getSerialNumber())
            .customerName(booking.getCustomerName())
            .recipientEmail(booking.getCustomerEmail())
            .buyerNotes(booking.getBuyerNotes())
            .bccEmails(Set.of(applicationPropertyService.findByName("mail.ticket.copy").propertyValue))
            .tickets(tickets.stream()
                .map(ticket -> ofTicketWithQrCode(ticket,
                    ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
                .collect(toList()))
            .sightEventPdfAttachmentsPaths(getDistinctPdfsForTickets(tickets))
            .build()
        );
        return ModelObjectsToDTOConverter.ofEmailSendingReport(report);
      } catch (Exception e) {
        logger.log(Level.ERROR, e.getLocalizedMessage());
        throw new EmailSendingException("Wystąpił błąd podczas wysyłania maila z kopią biletów.");
      }
    } else {
      Partner partner = loggedUser.getPartner();
      tickets =
          booking.getTickets().stream().filter(t -> t.getTicketPool().getTicketPoolDefinition()
              .getSightEvent().getPartner().equals(partner)).collect(toList());

      // sending email to buyer and in bcc to helpdesk and partner
      try {
        EmailSendingReport report = emailService.sendEmailWithQrCodes(BookingMarkedAsBoughtEvent.builder()
            .p24Currency(booking.getP24Currency())
            .hash(booking.getSerialNumber())
            .customerName(booking.getCustomerName())
            .recipientEmail(booking.getCustomerEmail())
            .buyerNotes(booking.getBuyerNotes())
            .bccEmails(Set.of(partner.getEmail(), applicationPropertyService.findByName("mail.ticket.copy").propertyValue))
            .tickets(tickets.stream()
                .map(ticket -> ofTicketWithQrCode(ticket,
                    ticket.encodeSerialNumberAsQrCode(qrCodeWidth, qrCodeHeight)))
                .collect(toList()))
            .sightEventPdfAttachmentsPaths(getDistinctPdfsForTickets(tickets))
            .build()
        );
        return ModelObjectsToDTOConverter.ofEmailSendingReport(report);
      } catch (Exception e) {
        logger.log(Level.ERROR, e.getLocalizedMessage());
        throw new EmailSendingException("Wystąpił błąd podczas wysyłania maila z kopią biletów.");
      }
    }
  }

  /**
   * Available to BOOK
   */
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
