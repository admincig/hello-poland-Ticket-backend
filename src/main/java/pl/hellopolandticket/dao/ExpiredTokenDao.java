package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.auth.ExpiredToken;

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
        .setParameter("token", token).getResultStream().findFirst();
  }

  public List<ExpiredToken> findAll() {
    return entityManager.createQuery("from ExpiredToken expiredToken", ExpiredToken.class)
        .getResultList();
  }

  public void remove(ExpiredToken expiredToken) {
    entityManager.remove(expiredToken);
  }
}
