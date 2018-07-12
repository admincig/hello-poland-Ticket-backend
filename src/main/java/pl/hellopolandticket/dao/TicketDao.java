package pl.hellopolandticket.dao;


import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

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
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  public Ticket findBySerialNumber(String serialNumber) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.serialNumber=:serialNumber", Ticket.class)
        .setParameter("serialNumber", serialNumber)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  public Long countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(Long ticketPoolId,
      List<Status> statuses) {
    return entityManager
        .createQuery(
            "SELECT COUNT(ticket) from Ticket ticket JOIN ticket.ticketDefinition ticketDefinition JOIN ticketDefinition.ticketPool ticketPool where ticketPool.id=:ticketPoolId AND ticket.status IN :statuses",
            Long.class)
        .setParameter("ticketPoolId", ticketPoolId)
        .setParameter("statuses", statuses)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}