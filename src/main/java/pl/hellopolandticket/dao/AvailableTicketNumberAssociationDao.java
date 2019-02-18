package pl.hellopolandticket.dao;

import java.util.ArrayList;
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

  public AvailableTicketNumberAssociation persist(AvailableTicketNumberAssociation bo) {
    entityManager.persist(bo);
    entityManager.flush();
    return bo;
  }

  public List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    List<AvailableTicketNumberAssociation> atp = entityManager.createQuery(
        "from AvailableTicketNumberAssociation a where a.ticketPool.ticketPoolDefinition = :tpd",
        AvailableTicketNumberAssociation.class).setParameter("tpd", tpd).getResultList();

    List<AvailableTicketNumberAssociation> atpd = entityManager
        .createQuery("from AvailableTicketNumberAssociation a where a.ticketPoolDefinition = :tpd",
            AvailableTicketNumberAssociation.class)
        .setParameter("tpd", tpd).getResultList();

    List<AvailableTicketNumberAssociation> atpdCopy = new ArrayList<>(atpd);
    List<AvailableTicketNumberAssociation> result = new ArrayList<>();

    for (AvailableTicketNumberAssociation a1 : atp) {
      for (AvailableTicketNumberAssociation a2 : atpd) {
        if (a1.getTicketDefinition().equals(a2.getTicketDefinition())) {
          result.add(a1);
          atpdCopy.remove(a2);
          continue;
        }
      }
    }
    result.addAll(atpdCopy);
    return result;
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
