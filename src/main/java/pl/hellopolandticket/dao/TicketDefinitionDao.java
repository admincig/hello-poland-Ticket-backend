package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Stateless
@LocalBean
public class TicketDefinitionDao {

  @PersistenceContext
  private EntityManager entityManager;

  public TicketDefinition persist(TicketDefinition ticketDefinition) {
    entityManager.persist(ticketDefinition);

    return ticketDefinition;
  }

  public TicketDefinition findById(Long ticketDefinitionId) {
    return entityManager
        .createQuery("from TicketDefinition ticketDefinition where ticketDefinition.id=:id",
            TicketDefinition.class)
        .setParameter("id", ticketDefinitionId)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

}