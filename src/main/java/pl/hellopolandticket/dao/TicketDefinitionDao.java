package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketDefinitionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public TicketDefinition persist(TicketDefinition ticketDefinition) {
    entityManager.persist(ticketDefinition);
    entityManager.flush();

    return ticketDefinition;
  }

  public TicketDefinition findById(Long ticketDefinitionId) {
    return entityManager
        .createQuery("from TicketDefinition ticketDefinition where ticketDefinition.id=:id",
            TicketDefinition.class)
        .setParameter("id", ticketDefinitionId)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public void merge(TicketDefinition ticketDefinition) {
    entityManager.merge(ticketDefinition);
  }

}