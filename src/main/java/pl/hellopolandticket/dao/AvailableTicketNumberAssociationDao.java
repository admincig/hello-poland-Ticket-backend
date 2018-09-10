package pl.hellopolandticket.dao;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class AvailableTicketNumberAssociationDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public AvailableTicketNumberAssociation persiste(AvailableTicketNumberAssociation bo) {
    entityManager.persist(bo);
    entityManager.flush();
    return bo;
  }

  public List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    return entityManager
        .createQuery("from AvailableTicketNumberAssociation a where a.ticketPoolDefinition = :tpd",
            AvailableTicketNumberAssociation.class)
        .setParameter("tpd", tpd).getResultList();
  }

  public List<AvailableTicketNumberAssociation> getForTicketPool(TicketPool tp) {
    return entityManager
        .createQuery("from AvailableTicketNumberAssociation a where a.ticketPool = :tp",
            AvailableTicketNumberAssociation.class)
        .setParameter("tp", tp).getResultList();
  }

  public AvailableTicketNumberAssociation findForTicketPoolDefinitionAndTicketDefinition(
      TicketPoolDefinition tpd, TicketDefinition td) {
    return entityManager.createQuery(
        "from AvailableTicketNumberAssociation a where a.ticketPoolDefinition = :tpd and a.ticketDefinition = :td",
        AvailableTicketNumberAssociation.class).setParameter("tpd", tpd).setParameter("td", td)
        .getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public AvailableTicketNumberAssociation findForTicketPoolAndTicketDefinition(TicketPool tp,
      TicketDefinition td) {
    return entityManager.createQuery(
        "from AvailableTicketNumberAssociation a where a.ticketPool = :tp and a.ticketDefinition = :td",
        AvailableTicketNumberAssociation.class).setParameter("tp", tp).setParameter("td", td)
        .getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public AvailableTicketNumberAssociation update(AvailableTicketNumberAssociation bo) {
    entityManager.merge(bo);
    entityManager.flush();
    return bo;
  }

}
