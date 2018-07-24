package pl.hellopolandticket.dao;

import static java.util.stream.Collectors.toList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
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
    return entityManager
        .createQuery("from TicketPool ticketPool where ticketPool.id=:id", TicketPool.class)
        .setParameter("id", ticketPoolId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<TicketPool> findAll() {
    return entityManager.createQuery("from TicketPool ticketPool", TicketPool.class)
        .getResultStream().collect(toList());
  }
}
