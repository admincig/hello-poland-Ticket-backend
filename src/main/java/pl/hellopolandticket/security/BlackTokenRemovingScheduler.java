package pl.hellopolandticket.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.BlackTokenDao;
import pl.hellopolandticket.model.BlackToken;
import pl.hellopolandticket.service.ApplicationPropertyService;

@Slf4j
@Singleton
public class BlackTokenRemovingScheduler {

  private static final String JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY = "jwt.accessTokenSecretKey";
  private static final String JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY = "jwt.refreshTokenSecretKey";

  @Inject
  private BlackTokenDao blackTokenDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Schedule(hour = "1", minute = "5", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  public void run() {
    List<BlackToken> blackTokens = blackTokenDao.findAll();

    for (BlackToken blackToken : blackTokens) {
      validateAccessTokenIsStillValid(blackToken);
      validateRefreshTokenStillValid(blackToken);
    }
  }

  private void validateAccessTokenIsStillValid(BlackToken blackToken) {
    String accessTokenSecretKey = applicationPropertyService
        .findByName(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY)
        .getPropertyValue();

    validateTokenStillValid(accessTokenSecretKey, blackToken);
  }

  private void validateRefreshTokenStillValid(BlackToken blackToken) {
    String refreshTokenSecretKey = applicationPropertyService
        .findByName(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY)
        .getPropertyValue();

    validateTokenStillValid(refreshTokenSecretKey, blackToken);
  }

  private void validateTokenStillValid(String secretKey, BlackToken blackToken) {
    try {
      Jwts.parser().setSigningKey(secretKey).parse(blackToken.getToken());
    } catch (SignatureException ignored) {

    } catch (ExpiredJwtException e) {
      blackTokenDao.remove(blackToken);
    }
  }
}
