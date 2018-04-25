package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.BlackTokenDao;
import pl.hellopolandticket.model.BlackToken;

@Stateless
@LocalBean
public class BlackTokenService {

  @Inject
  private BlackTokenDao blackTokenDao;

  public BlackToken addTokenToBlackList(String token) {
    BlackToken blackToken = BlackToken.builder()
        .token(token)
        .build();

    return blackTokenDao.persist(blackToken);
  }

  public boolean isTokenInBlackList(String token) {
    return blackTokenDao.findByToken(token).isPresent();
  }
}
