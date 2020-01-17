package pl.hellopolandticket.dao;

import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
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
        .setParameter("id", ticketDefinitionId).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public void merge(TicketDefinition ticketDefinition) {
    entityManager.merge(ticketDefinition);
  }

  public List<TicketDefinition> getUndeletedList(Partner partner) {
    return entityManager.createQuery(
        "from TicketDefinition ticketDefinition where "
            + "ticketDefinition.deleted=false and "
            + "ticketDefinition.partner=:partner order by ticketDefinition.id desc",
        TicketDefinition.class).setParameter("partner", partner).getResultList();
  }

  public List<TicketDefinition> getUndeletedList(List<Long> atnaIds) {
    if (atnaIds == null || atnaIds.isEmpty()) {
      return entityManager.createQuery("from TicketDefinition ticketDefinition where "
          + "ticketDefinition.deleted=false"
          + " order by ticketDefinition.id desc", TicketDefinition.class).getResultList();
    } else {
      return entityManager.createQuery(
          "from AvailableTicketNumberAssociation "
              + "where ticketDefinition.deleted=false "
              + "and id in (:ids)"
              + "order by ticketDefinition.id desc",
          AvailableTicketNumberAssociation.class)
          .setParameter("ids", atnaIds)
          .getResultStream()
          .map(atna -> {
            TicketDefinition td = atna.getTicketDefinition();
            entityManager.detach(td);
            td.setInterestingAtna(atna);
            return td;
          })
          .collect(Collectors.toList());
    }
  }

}
