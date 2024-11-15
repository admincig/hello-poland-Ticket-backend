package pl.hellopolandticket.dao;

import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import java.util.Date;
import java.util.List;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class BookingDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public Booking persist(Booking booking) {
    entityManager.persist(booking);
    entityManager.flush();

    return booking;
  }

  public Booking findBySerialNumber(String serialNumber) {
    return entityManager
        .createQuery("from Booking booking where booking.serialNumber=:serialNumber", Booking.class)
        .setParameter("serialNumber", serialNumber).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<Booking> findBookingsExceededMaxBookingTime(Date maxBookingTimeEarlier) {
    return entityManager
        .createQuery("from Booking booking where booking.status=:status AND booking.date<:date",
            Booking.class)
        .setParameter("date", maxBookingTimeEarlier).setParameter("status", BOOKED).getResultList();
  }
}
