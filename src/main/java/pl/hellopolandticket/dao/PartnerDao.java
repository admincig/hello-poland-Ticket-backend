package pl.hellopolandticket.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class PartnerDao {

  @PersistenceContext
  private EntityManager entityManager;

  @Inject
  private ExceptionFactory exceptionFactory;

  public Optional<Partner> findById(Long id) {
    return Optional.ofNullable(entityManager.find(Partner.class, id));
  }

  public Partner findByUserEmail(String email) {
    return entityManager
        .createQuery("select user.partner from User user where lower(user.email) = :email",
            Partner.class)
        .setParameter("email", email.toLowerCase()).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public Partner persist(Partner partner) {
    entityManager.persist(partner);

    return partner;
  }

  public void flush() {
    entityManager.flush();
  }

  public Partner findByName(String name) {
    return entityManager.createQuery("from Partner partner where partner.name=:name", Partner.class)
        .setParameter("name", name).getResultStream().findFirst().orElse(null);
  }

  public Partner findByEmail(String email) {
    return entityManager
        .createQuery("from Partner partner where lower(partner.email)=:email", Partner.class)
        .setParameter("email", email.toLowerCase())
        .getResultStream()
        .findFirst()
        .orElse(null);
  }

  public void removeNewCreatedPartner(Partner partner) {
    entityManager.remove(partner);
  }
}
