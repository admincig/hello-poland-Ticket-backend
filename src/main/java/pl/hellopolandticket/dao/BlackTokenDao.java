package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.BlackToken;

@Stateless
@LocalBean
public class BlackTokenDao {

  @PersistenceContext
  private EntityManager entityManager;

  public BlackToken persist(BlackToken token) {
    entityManager.persist(token);

    return token;
  }

  public Optional<BlackToken> findByToken(String token) {
    return entityManager
        .createQuery("from BlackToken blackToken where blackToken.token=:token", BlackToken.class)
        .setParameter("token", token)
        .getResultStream()
        .findFirst();
  }

  public List<BlackToken> findAll() {
    return entityManager
        .createQuery("from BlackToken blackToken", BlackToken.class)
        .getResultList();
  }

  public void remove(BlackToken blackToken) {
    entityManager.remove(blackToken);
  }
}
