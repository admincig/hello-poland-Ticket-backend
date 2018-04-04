package pl.hellopolandticket.dao;

import static pl.hellopolandticket.model.Ticket.Status.BOOKED;

import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.Ticket.Status;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

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

  public Long countTicketsBySightIdAndTicketStatusInTicketStatuses(Long sightId,
      List<Status> statuses) {
    return entityManager
        .createQuery(
            "SELECT COUNT(ticket) from Ticket ticket where ticket.sight.id=:sightId AND ticket.status IN :statuses",
            Long.class)
        .setParameter("sightId", sightId)
        .setParameter("statuses", statuses)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

  public List<Ticket> findBookedExceededMaxBookingTime(Date maxBookingTimeEarlier) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.status=:status AND ticket.date<:date",
            Ticket.class)
        .setParameter("date", maxBookingTimeEarlier)
        .setParameter("status", BOOKED)
        .getResultList();
  }

  public List<Ticket> findByIdsIn(List<Long> ticketIds) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.id IN :ticketIds",
            Ticket.class)
        .setParameter("ticketIds", ticketIds)
        .getResultList();
  }
}