package pl.hellopolandticket.dao;

import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.auth.User;

@Stateless
@LocalBean
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<User> findByEmail(String email) {
    return entityManager.createQuery("from User user where user.email=:email", User.class)
        .setParameter("email", email).getResultStream().findFirst();
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

}
