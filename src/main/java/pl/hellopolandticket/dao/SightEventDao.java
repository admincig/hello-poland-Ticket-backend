package pl.hellopolandticket.dao;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class SightEventDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public SightEvent persist(SightEvent sightEvent) {
    entityManager.persist(sightEvent);
    entityManager.flush();

    return sightEvent;
  }

  public SightEvent findById(Long sightEventId) {
    return entityManager
        .createQuery("from SightEvent sightEvent where sightEvent.id=:id", SightEvent.class)
        .setParameter("id", sightEventId)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public List<SightEvent> findBySightEventIdsIn(List<Long> sightIds) {
    return entityManager
        .createQuery("from SightEvent sightEvent WHERE sightEvent.id IN :sightIds",
            SightEvent.class)
        .setParameter("sightIds", sightIds)
        .getResultStream()
        .collect(toList());
  }

  public List<SightEvent> findAll() {
    return entityManager
        .createQuery("from SightEvent sightEvent", SightEvent.class)
        .getResultStream()
        .collect(toList());
  }
}
