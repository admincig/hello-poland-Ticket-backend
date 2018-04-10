package pl.hellopolandticket.dao;

import static pl.hellopolandticket.model.Status.BOOKED;

import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Stateless
@LocalBean
public class BookingDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Booking persist(Booking booking) {
    entityManager.persist(booking);
    entityManager.flush();

    return booking;
  }

  public Booking findById(Long bookingId) {
    return entityManager
        .createQuery("from Booking booking where booking.id=:id", Booking.class)
        .setParameter("id", bookingId)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

  public List<Booking> findBookingsExceededMaxBookingTime(Date maxBookingTimeEarlier) {
    return entityManager
        .createQuery("from Booking booking where booking.status=:status AND booking.date<:date",
            Booking.class)
        .setParameter("date", maxBookingTimeEarlier)
        .setParameter("status", BOOKED)
        .getResultList();
  }
}