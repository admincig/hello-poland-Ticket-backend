package pl.hellopolandticket.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.sightevent.OpeningHours;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class OpeningHoursDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public OpeningHours persist(OpeningHours openingHours) {
    entityManager.persist(openingHours);
    entityManager.flush();
    return openingHours;
  }

  public void remove(OpeningHours openingHours) {
    entityManager.remove(openingHours);
    entityManager.flush();
  }

}
