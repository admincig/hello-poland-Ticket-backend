package pl.hellopolandticket.security;

import java.util.List;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import pl.hellopolandticket.dao.ExpiredTokenDao;
import pl.hellopolandticket.model.auth.ExpiredToken;
import pl.hellopolandticket.service.ApplicationPropertyService;

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
    String accessTokenSecretKey =
        applicationPropertyService.findByName(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY).propertyValue;

    validateTokenStillValid(accessTokenSecretKey, expiredToken);
  }

  private void validateRefreshTokenStillValid(ExpiredToken expiredToken) {
    String refreshTokenSecretKey =
        applicationPropertyService.findByName(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY).propertyValue;

    validateTokenStillValid(refreshTokenSecretKey, expiredToken);
  }

  private void validateTokenStillValid(String secretKey, ExpiredToken expiredToken) {
    try {
      Jwts.parser().setSigningKey(secretKey).build().parse(expiredToken.getToken());
    } catch (ExpiredJwtException e) {
      expiredTokenDao.remove(expiredToken);
    }
  }

  private boolean isInvalidToken(int numberOfThrownSignatureExceptions) {
    return numberOfThrownSignatureExceptions == 2;
  }
}
