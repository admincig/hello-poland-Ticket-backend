package pl.hellopolandticket.service;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.hellopolandticket.dao.ExpiredTokenDao;
import pl.hellopolandticket.model.auth.ExpiredToken;

@Stateless
@LocalBean
public class ExpiredTokenService {

  @Inject
  private ExpiredTokenDao expiredTokenDao;

  public ExpiredToken addTokenToExpiredTokensList(String token) {
    ExpiredToken expiredToken = ExpiredToken.builder().token(token).build();

    return expiredTokenDao.persist(expiredToken);
  }

  public boolean isTokenInExpiredTokensList(String token) {
    return expiredTokenDao.findByToken(token).isPresent();
  }
}
