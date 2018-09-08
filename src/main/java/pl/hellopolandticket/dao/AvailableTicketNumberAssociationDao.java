package pl.hellopolandticket.dao;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

@Stateless
@LocalBean
public class AvailableTicketNumberAssociationDao {

  @PersistenceContext
  private EntityManager entityManager;

  public AvailableTicketNumberAssociation persiste(AvailableTicketNumberAssociation bo) {
    entityManager.persist(bo);
    entityManager.flush();
    return bo;
  }

  public List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      Long ticketPoolDefinitionId) {
    return entityManager
        .createQuery(
            "from AvailableTicketNumberAssociation a where a.ticketPoolDefinition.id = :id",
            AvailableTicketNumberAssociation.class)
        .setParameter("id", ticketPoolDefinitionId).getResultList();
  }

  public List<AvailableTicketNumberAssociation> getForTicketPool(Long ticketPoolId) {
    return entityManager
        .createQuery("from AvailableTicketNumberAssociation a where a.ticketPool.id = :id",
            AvailableTicketNumberAssociation.class)
        .setParameter("id", ticketPoolId).getResultList();
  }

}
