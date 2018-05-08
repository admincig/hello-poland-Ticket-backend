package pl.hellopolandticket.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import java.util.List;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.ExpiredTokenDao;
import pl.hellopolandticket.model.ExpiredToken;
import pl.hellopolandticket.service.ApplicationPropertyService;

@Slf4j
@Singleton
public class ExpiredTokenRemovingScheduler {

  private static final String JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY = "jwt.accessTokenSecretKey";
  private static final String JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY = "jwt.refreshTokenSecretKey";

  @Inject
  private ExpiredTokenDao expiredTokenDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Schedule(hour = "*", minute = "*/5", second = "0", year = "*", dayOfMonth = "*", dayOfWeek = "*",
      persistent = false)
  public void run() {
    List<ExpiredToken> expiredTokens = expiredTokenDao.findAll();

    for (ExpiredToken expiredToken : expiredTokens) {
      int numberOfThrownSignatureExceptions = 0;
      try {
        validateAccessTokenIsStillValid(expiredToken);
      } catch (SignatureException e) {
        numberOfThrownSignatureExceptions++;
      }
      try {
        validateRefreshTokenStillValid(expiredToken);
      } catch (SignatureException e) {
        numberOfThrownSignatureExceptions++;
      }

      if (isInvalidToken(numberOfThrownSignatureExceptions)) {
        expiredTokenDao.remove(expiredToken);
      }
    }
  }

  private void validateAccessTokenIsStillValid(ExpiredToken expiredToken) {
    String accessTokenSecretKey = applicationPropertyService
        .findByName(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY)
        .getPropertyValue();

    validateTokenStillValid(accessTokenSecretKey, expiredToken);
  }

  private void validateRefreshTokenStillValid(ExpiredToken expiredToken) {
    String refreshTokenSecretKey = applicationPropertyService
        .findByName(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY)
        .getPropertyValue();

    validateTokenStillValid(refreshTokenSecretKey, expiredToken);
  }

  private void validateTokenStillValid(String secretKey, ExpiredToken expiredToken) {
    try {
      Jwts.parser().setSigningKey(secretKey).parse(expiredToken.getToken());
    } catch (ExpiredJwtException e) {
      expiredTokenDao.remove(expiredToken);
    }
  }

  private boolean isInvalidToken(int numberOfThrownSignatureExceptions) {
    return numberOfThrownSignatureExceptions == 2;
  }
}
