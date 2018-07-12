package pl.hellopolandticket.dao;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketPoolDefinitionDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public TicketPoolDefinition persist(TicketPoolDefinition ticketPoolDefinition) {
    entityManager.persist(ticketPoolDefinition);

    return ticketPoolDefinition;
  }

  public TicketPoolDefinition findById(Long ticketPoolDefinitionId) {
    return entityManager
        .createQuery(
            "from TicketPoolDefinition ticketPoolDefinition where ticketPoolDefinition.id=:id",
            TicketPoolDefinition.class)
        .setParameter("id", ticketPoolDefinitionId)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<TicketPoolDefinition> findAll() {
    return entityManager
        .createQuery("from TicketPoolDefinition ticketPoolDefinition", TicketPoolDefinition.class)
        .getResultStream()
        .collect(toList());
  }

  public List<TicketPoolDefinition> findByIdsIn(List<Long> ticketPoolDefinitionIds) {
    return entityManager
        .createQuery(
            "from TicketPoolDefinition ticketPoolDefinition WHERE ticketPoolDefinition.id IN :ticketPoolDefinitionIds",
            TicketPoolDefinition.class)
        .setParameter("ticketPoolDefinitionIds", ticketPoolDefinitionIds)
        .getResultStream()
        .collect(toList());
  }
}
