package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;
  @Inject
  private ExceptionFactory exceptionFactory;

  public Optional<User> findUserById(Long id) {
    return Optional.ofNullable(entityManager.find(User.class, id));
  }

  public Optional<User> findByEmail(String email) {
    return entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
        .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
  }

  public User findByEmailOrThrowException(String email) {
    return entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
        .setParameter("email", email.toLowerCase()).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public Optional<User> findByEmailWithAuthorities(String email) {
    Optional<User> optional =
        entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
            .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
    if (optional.isPresent()) {
      optional.get().getAuthorities().size();
    }
    return optional;
  }

  public Optional<User> findByEmailAndNotHidden(String email) {
    Optional<User> optional =
        entityManager
            .createQuery("from User user where lower(user.email) = :email and user.hidden=false",
                User.class)
            .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
    if (optional.isPresent()) {
      optional.get().getAuthorities().size();
    }
    return optional;
  }

  public Optional<User> findByToken(String token) {
    return entityManager.createQuery("from User user where user.token=:token", User.class)
        .setParameter("token", token).getResultStream().findFirst();
  }

  public Optional<User> findByTokenWithAuthorities(String token) {
    Optional<User> optional =
        entityManager.createQuery("from User user where user.token=:token", User.class)
            .setParameter("token", token).getResultStream().findFirst();
    if (optional.isPresent()) {
      optional.get().getAuthorities().size();
    }
    return optional;
  }

  public User persist(User user) {
    entityManager.persist(user);
    return user;
  }

  public List<User> getUsersByCurrentUserAndRole(User loggedUser, String role) {
    return entityManager.createQuery(
        "from User u where u.partner = :partner and u != :loggedUser and :role in elements(u.authorities)",
        User.class).setParameter("partner", loggedUser.getPartner())
        .setParameter("loggedUser", loggedUser).setParameter("role", role).getResultList();
  }

  public User getUserByRoleForCurrentPartner(long userId, Partner partner, String role) {
    return entityManager.createQuery(
        "from User u where u.id = :id and u.partner = :partner and :role in elements(u.authorities)",
        User.class).setParameter("id", userId).setParameter("partner", partner)
        .setParameter("role", role).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public User getUserForCurrnetPartner(long userId, Partner partner) {
    return entityManager
        .createQuery("from User u where u.id = :id and u.partner = :partner", User.class)
        .setParameter("id", userId).setParameter("partner", partner).getResultStream().findFirst()
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public User updateUser(User user) {
    return entityManager.merge(user);
  }

}
