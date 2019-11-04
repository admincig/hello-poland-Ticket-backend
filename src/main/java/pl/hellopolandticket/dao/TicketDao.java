package pl.hellopolandticket.dao;


import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketDao {

  @PersistenceContext
  private static EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public List<Ticket> persist(List<Ticket> tickets) {
    for (Ticket ticket : tickets) {
      entityManager.persist(ticket);
    }

    return tickets;
  }

  public Ticket findById(Long ticketId) {
    return entityManager.createQuery("from Ticket ticket where ticket.id=:id", Ticket.class)
        .setParameter("id", ticketId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  // TODO
  public Ticket findBySerialNumber(String serialNumber) {
    return entityManager
        .createQuery("from Ticket ticket where ticket.serialNumber=:serialNumber", Ticket.class)
        .setParameter("serialNumber", serialNumber).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  public static boolean isUniqueSerialNumber(String uuid) {
    return !entityManager
        .createQuery(
            "select exists (select t from Ticket t where t.serialNumber like':shortUUID%')",
            Boolean.class)
        .setParameter("shortUUID", getShortUUID(uuid))
        .getSingleResult();
  }

  private static String getShortUUID(String uuid) {
    return uuid.substring(0, 6);
  }

  public Long countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(Long ticketPoolId,
      List<Status> statuses) {
    return entityManager.createQuery(
        "SELECT COUNT(ticket) from Ticket ticket JOIN ticket.ticketDefinition ticketDefinition JOIN ticketDefinition.ticketPool ticketPool where ticketPool.id=:ticketPoolId AND ticket.status IN :statuses",
        Long.class).setParameter("ticketPoolId", ticketPoolId).setParameter("statuses", statuses)
        .getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public SightEvent findSightEventForTicket(Long id) {
    return entityManager.createQuery(
        "SELECT tpd.sightEvent from Ticket t JOIN t.ticketPool tp JOIN tp.ticketPoolDefinition tpd where t.id = :ticketId",
        SightEvent.class).setParameter("ticketId", id).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}
