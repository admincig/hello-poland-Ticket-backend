package pl.hellopolandticket.service;

import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketPoolQuantityMonitoringService extends ServiceSuperclass {

  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private EmailService emailService;

  /**
   * All methods in BookingService are operating on 'availableTicketNumber' which tells us how many
   * tickets are left to BOOK. We want to check how many there are tickets to BUY.
   */
  public void checkTicketNumberLeftToBuy(Ticket ticket) {
    Long numberOfAllTicketsYouCanBuyFromPool =
        Long.valueOf(ticket.getTicketPool().getTicketPoolDefinition().getAvailableTicketsNumber());

    if (numberOfAllTicketsYouCanBuyFromPool != -1 && numberOfAllTicketsYouCanBuyFromPool > 0) {
      Long ticketsNumberLeftToBuy = numberOfTicketsLeftToBuy(ticket);
      if (shouldInformByEmail(ticketsNumberLeftToBuy)) {
        informPartnerAboutTicketsNumberLeftToBuyRunningOut(ticket, ticketsNumberLeftToBuy);
      }
    }
  }

  private Long numberOfTicketsLeftToBuy(Ticket ticket) {
    Long allTicketsYouCanBuyFromPool =
        Long.valueOf(ticket.getTicketPool().getTicketPoolDefinition().getAvailableTicketsNumber());

    Long countOfAllTickets = Long.valueOf(ticket.getTicketPool().getTickets().stream().count());
    Long countOfBookedTickets = ticket.getTicketPool().getTickets().stream()
        .filter(t -> Status.BOOKED.equals(t.getStatus()))
        .count();

    Long countOfTicketsYouCannotBuy = countOfAllTickets - countOfBookedTickets;
    return allTicketsYouCanBuyFromPool - countOfTicketsYouCannotBuy;
  }

  private boolean shouldInformByEmail(Long number) {
    Long three = 3L;
    Long zero = 0L;
    return three.equals(number) || zero.equals(number);
  }

  private void informPartnerAboutTicketsNumberLeftToBuyRunningOut(Ticket ticket,
      Long ticketsNumberLeftToBuy) {

    emailsToInformAboutTicketsRunningOut(ticket).forEach(email -> {
      try {
        emailService.sendSimpleEmail(email,
            mailTitle(ticketsNumberLeftToBuy,
                ticket.getTicketPool().getTicketPoolDefinition().getSightEvent().getName()),
            mailContent(ticket, ticketsNumberLeftToBuy));
        logger.log(Logger.Level.INFO, "Informing " + email + " about tickets from ticket pool[id="
            + ticket.getTicketPool().getId() + "] running out.");
      } catch (MessagingException | UnsupportedEncodingException e) {
        throw exceptionFactory.emailSendingRollbackException();
      }
    });
  }

  private String mailTitle(Long ticketsNumberLeftToBuy, String sightEventName) {
    if (ticketsNumberLeftToBuy > 0) {
      return "Wyczerpują się bilety na ofertę \"" + sightEventName + "\"";
    }
    return "Bilety na ofertę " + sightEventName + " zostały wyprzedane.";
  }

  private String mailContent(Ticket ticket, Long ticketsNumberLeftToBuy) {
    TicketPool pool = ticket.getTicketPool();
    String poolName = pool.getName();
    String startDate = pool.getStartDate().toString();
    String endDate = pool.getEndDate().toString();

    TicketPoolDefinition definition = pool.getTicketPoolDefinition();
    String sightEventName = definition.getSightEvent().getName();
    String runoutDate = new Date().toString();

    StringBuffer buff = new StringBuffer("");
    buff.append("Dzień dobry, \r\n\r\n");
    buff.append("Liczba pozostałych biletów: " + ticketsNumberLeftToBuy + " \r\n");
    buff.append("Na ofertę: " + sightEventName + " \r\n");
    buff.append("Z puli biletów: " + poolName + " \r\n");
    buff.append("Dostępną w terminie od: " + startDate + " , do: " + endDate + " \r\n");
    buff.append("Rodzaje biletów: \r\n");

    HashMap<String, Long> ticketNamesMap = ticketNamesWithQuantityBoughtFromPool(ticket);
    for (Map.Entry<String, Long> entry : ticketNamesMap.entrySet()) {
      buff.append("     " + entry.getKey() + " - wykupiono: " + entry.getValue() + " biletów \r\n");
    }

    buff.append("Data (prawie) wyczerpania puli: " + runoutDate + " \r\n");
    return buff.toString();
  }

  private HashMap<String, Long> ticketNamesWithQuantityBoughtFromPool(Ticket ticket) {
    Set<String> ticketNames =
        ticket.getTicketPool().getTicketPoolDefinition().getTicketDefinitions().stream()
            .map(td -> td.getName()).collect(Collectors.toSet());

    List<Ticket> list = ticket.getTicketPool().getTickets();
    HashMap<String, Long> map = new HashMap<>();
    for (String ticketName : ticketNames) {
      map.put(ticketName, countBoughtTicketsWithName(list, ticketName));
    }
    return map;
  }

  private Long countBoughtTicketsWithName(List<Ticket> list, String name) {
    return list.stream()
        .filter(ticket -> ticket.getName().equals(name))
        .filter(ticket -> Status.BOUGHT.equals(ticket.getStatus()))
        .count();
  }

  private Set<String> emailsToInformAboutTicketsRunningOut(Ticket ticket) {
    Set<String> all = new HashSet<>();
    all.add(properties.getProperty("mail.hellopoland.biuro"));
    all.add(ticket.getTicketDefinition().getPartner().getEmail());
    return all;
  }

}
