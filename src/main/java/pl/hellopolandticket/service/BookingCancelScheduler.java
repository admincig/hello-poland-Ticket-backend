package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static pl.hellopolandticket.model.Ticket.Status.INVALID;

import java.util.Calendar;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;

@Slf4j
@Singleton
public class BookingCancelScheduler {

  private final static String TICKET_BOOKED_TIME_TO_BUY_PROPERTY = "ticket.booked.timeToBuy";

  @Inject
  private TicketDao ticketDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Schedule(hour = "*", minute = "*/5", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  public void run() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, -valueOf(
        applicationPropertyService.findByName(TICKET_BOOKED_TIME_TO_BUY_PROPERTY)
            .getPropertyValue()));

    List<Ticket> expiredBookedTickets = ticketDao
        .findBookedExceededMaxBookingTime(calendar.getTime());

    for (Ticket ticket : expiredBookedTickets) {
      log.debug("For the ticket {} changed status to invalid. The ticket wasn't bought.",
          ticket);

      ticket.setStatus(INVALID);

      Sight sight = ticket.getSight();
      sight.increaseAvailableTicketsNumber();
    }
  }
}