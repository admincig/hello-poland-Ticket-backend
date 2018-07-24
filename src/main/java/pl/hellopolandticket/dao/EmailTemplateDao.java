package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
        .setParameter("name", name)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}
