package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.config.ApplicationProperty;

@Stateless
@LocalBean
public class ApplicationPropertyDao {

  @PersistenceContext
  private EntityManager entityManager;

  public ApplicationProperty persist(ApplicationProperty applicationProperty) {
    entityManager.persist(applicationProperty);

    return applicationProperty;
  }

  public Optional<ApplicationProperty> findByPropertyName(String propertyName) {
    return entityManager.createQuery(
        "from ApplicationProperty applicationProperty where applicationProperty.propertyName=:propertyName",
        ApplicationProperty.class).setParameter("propertyName", propertyName).getResultStream()
        .findFirst();
  }

  public List<ApplicationProperty> findAll() {
    return entityManager
        .createQuery("from ApplicationProperty applicationProperty", ApplicationProperty.class)
        .getResultList();
  }

  public void remove(ApplicationProperty applicationProperty) {
    entityManager.remove(applicationProperty);
  }
}
