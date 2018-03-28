package pl.hellopolandticket.dao;

import static pl.hellopolandticket.model.Ticket.Status.BOOKED;

import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Ticket;

@Stateless
@LocalBean
public class TicketDao {

  @PersistenceContext
  private EntityManager entityManager;

  public List<Ticket> persist(List<Ticket> tickets) {
    for (Ticket ticket : tickets) {
      entityManager.persist(ticket);
    }

    return tickets;
  }

  public List<Ticket> findBookedExceededMaxBookingTime(Date maxBookingTimeEarlier) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.status=:status AND ticket.date<:date",
            Ticket.class)
        .setParameter("date", maxBookingTimeEarlier)
        .setParameter("status", BOOKED)
        .getResultList();
  }
}
