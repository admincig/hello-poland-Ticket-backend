package pl.hellopolandticket.dao;

import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.User;

@Stateless
@LocalBean
public class UserDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<User> findByUsername(String username) {
    return entityManager
        .createQuery("from User user where user.username=:username", User.class)
        .setParameter("username", username)
        .getResultStream()
        .findFirst();
  }

  public Optional<User> findByEmail(String email) {
    return entityManager
        .createQuery("from User user where user.email=:email", User.class)
        .setParameter("email", email)
        .getResultStream()
        .findFirst();
  }

  public User persist(User user) {
    entityManager.persist(user);

    return user;
  }

}
