package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Stateless
@LocalBean
public class SightDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Sight persist(Sight sight) {
    entityManager.persist(sight);

    return sight;
  }

  public Sight findBySightName(String sightName) {
    return entityManager
        .createQuery("from Sight sight where sight.name=:name", Sight.class)
        .setParameter("name", sightName)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Sight findById(Long sightId) {
    return entityManager
        .createQuery("from Sight sight where sight.id=:id", Sight.class)
        .setParameter("id", sightId)
        .getResultStream()
        .findFirst()
        .orElseThrow(ResourceNotFoundException::new);
  }

}