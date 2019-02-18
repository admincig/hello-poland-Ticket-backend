package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;
  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private PartnerDao partnerDao;

  public Optional<User> findByEmail(String email) {
    return entityManager.createQuery("from User user where user.email=:email", User.class)
        .setParameter("email", email).getResultStream().findFirst();
  }

  public User findByEmailOrThrowException(String email) {
    return entityManager.createQuery("from User user where user.email=:email", User.class)
        .setParameter("email", email).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public Optional<User> findByEmailWithAuthorities(String email) {
    Optional<User> optional =
        entityManager.createQuery("from User user where user.email=:email", User.class)
            .setParameter("email", email).getResultStream().findFirst();
    if (optional.isPresent()) {
      optional.get().getAuthorities().size();
    }
    return optional;
  }

  public Optional<User> findByEmailAndNotHidden(String email) {
    Optional<User> optional = entityManager
        .createQuery("from User user where user.email=:email and user.hidden=false", User.class)
        .setParameter("email", email).getResultStream().findFirst();
    if (optional.isPresent()) {
      optional.get().getAuthorities().size();
    }
    return optional;
  }

  public Optional<User> findByToken(String token) {
    return entityManager.createQuery("from User user where user.token=:token", User.class)
        .setParameter("token", token).getResultStream().findFirst();
  }

  public User persist(User user) {
    entityManager.persist(user);

    return user;
  }

  public List<User> getUsersByCurrentUserAndRole(User loggedUser, String role) {
    return entityManager.createQuery(
        "from User u where u.partner.id = :partnerId and u.id != :partnerId and :role in elements(u.authorities)",
        User.class).setParameter("partnerId", loggedUser.getId()).setParameter("role", role)
        .getResultList();
  }

}
