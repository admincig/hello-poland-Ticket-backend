package pl.hellopolandticket.dao;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketStatus;
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

  public Long countTicketsBySightIdAndTicketStatusNotInTicketStatuses(Long sightId,
      List<TicketStatus> ticketStatuses) {
    return entityManager
        .createQuery(
            "SELECT COUNT(ticket) from Ticket ticket where ticket.sight.id=:sightId AND ticket.ticketStatus NOT IN :ticketStatuses",
            Long.class)
        .setParameter("sightId", sightId)
        .setParameter("ticketStatuses", ticketStatuses)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Long countTicketsBySightIdAndTicketStatusInTicketStatuses(Long sightId,
      List<TicketStatus> ticketStatuses) {
    return entityManager
        .createQuery(
            "SELECT COUNT(ticket) from Ticket ticket where ticket.sight.id=:sightId AND ticket.ticketStatus IN :ticketStatuses",
            Long.class)
        .setParameter("sightId", sightId)
        .setParameter("ticketStatuses", ticketStatuses)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }
}