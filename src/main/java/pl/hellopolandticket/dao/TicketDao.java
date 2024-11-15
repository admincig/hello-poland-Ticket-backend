package pl.hellopolandticket.dao;


import java.util.List;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;

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
    return entityManager.createQuery("from Ticket ticket where ticket.id=:id", Ticket.class)
        .setParameter("id", ticketId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  /**
   * 
   * @param serialNumber - whole serial number or part of it. At least first 7 characters are
   *        needed.
   * @return Returns ticket by its whole serialNumber or at least first 7 characters.
   * 
   */
  public Ticket findBySerialNumber(String serialNumber) {
    if (serialNumber.length() < 7) {
      throw new ConflictingException(
          "Serial number for lookup needs to have at least first 7 characters");
    }
    return entityManager
        .createQuery("from Ticket where serialNumber like :serialNumber",
            Ticket.class)
        .setParameter("serialNumber", serialNumber + "%")
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.ticketNotFoundException());
  }

  /**
   * @param uuid - whole uuid
   * @return Returns true if first 7 characters of uuid are not used somewhere in database for
   *         tickets serial number.
   */
  public boolean isUniqueSerialNumber(String uuid) {
    return entityManager
        .createQuery(
            "from Ticket t where t.serialNumber like :shortUUID",
            Ticket.class)
        .setParameter("shortUUID", getShortUUID(uuid) + "%").getResultStream().findAny().isEmpty();
  }

  private String getShortUUID(String uuid) {
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
