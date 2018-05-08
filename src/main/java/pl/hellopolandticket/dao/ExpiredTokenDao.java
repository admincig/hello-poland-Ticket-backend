package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.ExpiredToken;

@Stateless
@LocalBean
public class ExpiredTokenDao {

  @PersistenceContext
  private EntityManager entityManager;

  public ExpiredToken persist(ExpiredToken token) {
    entityManager.persist(token);

    return token;
  }

  public Optional<ExpiredToken> findByToken(String token) {
    return entityManager
        .createQuery("from ExpiredToken expiredToken where expiredToken.token=:token",
            ExpiredToken.class)
        .setParameter("token", token)
        .getResultStream()
        .findFirst();
  }

  public List<ExpiredToken> findAll() {
    return entityManager
        .createQuery("from ExpiredToken expiredToken", ExpiredToken.class)
        .getResultList();
  }

  public void remove(ExpiredToken expiredToken) {
    entityManager.remove(expiredToken);
  }
}
