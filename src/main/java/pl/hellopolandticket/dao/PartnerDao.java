package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class PartnerDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public Partner findUserEmail(String email) {
    return entityManager
        .createQuery("from Partner partner JOIN partner.users user where user.email=:email",
            Partner.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}
