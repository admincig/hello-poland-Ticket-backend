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

  public Partner findByUserEmail(String email) {
    return entityManager
        .createQuery("from Partner partner JOIN partner.users user where user.email=:email",
            Partner.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public Partner persist(Partner partner) {
    entityManager.persist(partner);

    return partner;
  }

  public Partner findByToken(String token) {
    return entityManager
        .createQuery("from Partner partner where partner.token=:token", Partner.class)
        .setParameter("token", token)
        .getResultStream()
        .findFirst()
        .orElse(null);
  }

  public Partner findByName(String name) {
    return entityManager
        .createQuery("from Partner partner where partner.name=:name", Partner.class)
        .setParameter("name", name)
        .getResultStream()
        .findFirst()
        .orElse(null);
  }
}
