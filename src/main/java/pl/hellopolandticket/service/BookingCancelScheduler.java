package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.ticket.market.Booking;

@Slf4j
@Singleton
public class BookingCancelScheduler extends ServiceSuperclass {

  private final static String TICKET_BOOKED_TIME_TO_BUY_PROPERTY = "ticket.booked.timeToBuy";

  @Inject
  private BookingDao bookingDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Schedule(hour = "*", minute = "*/5", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  public void run() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, -valueOf(
        applicationPropertyService.findByName(TICKET_BOOKED_TIME_TO_BUY_PROPERTY).propertyValue));

    List<Booking> expiredBookings =
        bookingDao.findBookingsExceededMaxBookingTime(calendar.getTime());

    for (Booking booking : expiredBookings) {
      log.debug("For the booking {} changed status to invalid. The ticket wasn't bought.", booking);

      booking.makeInvalid();
    }
  }
}
