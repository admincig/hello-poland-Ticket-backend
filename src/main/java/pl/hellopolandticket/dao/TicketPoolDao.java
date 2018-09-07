package pl.hellopolandticket.dao;

import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketPoolDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;


  public TicketPool persist(TicketPool ticketPool) {
    entityManager.persist(ticketPool);
    entityManager.flush();

    return ticketPool;
  }

  public TicketPool findById(Long ticketPoolId) {
    return entityManager.createQuery("from TicketPool e where e.id=:id", TicketPool.class)
        .setParameter("id", ticketPoolId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<TicketPool> findAll() {
    return entityManager.createQuery("from TicketPool e", TicketPool.class).getResultList();
  }

  public TicketPool find(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    List<TicketPool> resultList = entityManager.createQuery(
        "from TicketPool e where e.ticketPoolDefinition=:poolDefinition and ((:requestedDate between e.startDate and e.endDate) or (e.endDate = null and e.startDate >= :requestedDate)) order by e.id desc",
        TicketPool.class).setParameter("requestedDate", requestedDate)
        .setParameter("poolDefinition", ticketPoolDefinition).getResultList();
    if (resultList.isEmpty()) {
      return null;
    }
    return resultList.get(0);
  }

  public TicketPool findBySightEventId(Long sightEventId) {
    return entityManager
        .createQuery("from TicketPool e where e.ticketPoolDefinition.sightEvent.id = :id",
            TicketPool.class)
        .setParameter("id", sightEventId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

}
