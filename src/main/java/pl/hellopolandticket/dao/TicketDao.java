package pl.hellopolandticket.dao;


import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Status;
import pl.hellopolandticket.model.Ticket;
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

  public Ticket findById(Long ticketId) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.id=:id", Ticket.class)
        .setParameter("id", ticketId)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Ticket findBySerialNumber(String serialNumber) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.serialNumber=:serialNumber", Ticket.class)
        .setParameter("serialNumber", serialNumber)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
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
}