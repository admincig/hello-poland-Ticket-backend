package pl.hellopolandticket.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.config.EmailTemplate;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class EmailTemplateDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public EmailTemplate findByName(String name) {
    return entityManager
        .createQuery("from EmailTemplate emailTemplate where emailTemplate.name=:name",
            EmailTemplate.class)
        .setParameter("name", name).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}
