package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Sight;

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
        .getSingleResult();
  }

}