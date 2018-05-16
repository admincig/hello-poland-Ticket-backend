package pl.hellopolandticket.dao;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class SightDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

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
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public Sight findById(Long sightId) {
    return entityManager
        .createQuery("from Sight sight where sight.id=:id", Sight.class)
        .setParameter("id", sightId)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<Sight> findByIdsIn(List<Long> sightIds) {
    return entityManager
        .createQuery("from Sight sight WHERE sight.id IN :sightIds", Sight.class)
        .setParameter("sightIds", sightIds)
        .getResultStream()
        .collect(toList());
  }

}