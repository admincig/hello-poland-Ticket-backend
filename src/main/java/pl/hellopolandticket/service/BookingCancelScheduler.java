package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import java.lang.System.Logger.Level;
import java.util.Calendar;
import java.util.List;
import javax.annotation.security.RunAs;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.ticket.market.Booking;

@Singleton
@RunAs(value = ROLE_ADMIN)
public class BookingCancelScheduler extends ServiceSuperclass {

  private final static String TICKET_BOOKED_TIME_TO_BUY_PROPERTY = "ticket.booked.timeToBuy";

  @Inject
  private BookingDao bookingDao;

  @Inject
  private BookingService service;

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
      logger.log(Level.DEBUG,
          "For the booking {} changed status to invalid. The ticket wasn't bought.", booking);
      service.makeInvalid(booking);
    }
  }

}
