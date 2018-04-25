package pl.hellopolandticket.security.jwt;

import static java.util.stream.Collectors.joining;
import static pl.hellopolandticket.security.jwt.TokenType.ACCESS_TOKEN;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.service.ApplicationPropertyService;

@Slf4j
@ApplicationScoped
public class TokenProvider {

  private static final String AUTHORITIES_KEY = "auth";
  private static final String JWT_ACCESS_TOKEN_VALIDITY_PROPERTY = "jwt.accessTokenValidityMillis";
  private static final String JWT_REFRESH_TOKEN_VALIDITY_PROPERTY = "jwt.refreshTokenValidityMillis";
  private static final String JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY = "jwt.accessTokenSecretKey";
  private static final String JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY = "jwt.refreshTokenSecretKey";

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  public String createToken(String username, Set<String> authorities, TokenType tokenType) {
    if (tokenType == ACCESS_TOKEN) {
      return createAccessToken(username, authorities);
    } else {
      return createRefreshToken(username, authorities);
    }
  }

  public JwtCredential getCredential(String token, TokenType tokenType) {
    if (tokenType == ACCESS_TOKEN) {
      return getAccessTokenCredential(token);
    } else {
      return getRefreshTokenCredential(token);
    }
  }

  public void validateToken(String authToken, TokenType tokenType) {
    if (tokenType == ACCESS_TOKEN) {
      validateAccessToken(authToken);
    } else {
      validateRefreshToken(authToken);
    }
  }

  private String createAccessToken(String username, Set<String> authorities) {
    long now = (new Date()).getTime();
    long accessTokenValidity = getTokenValidity(JWT_ACCESS_TOKEN_VALIDITY_PROPERTY);
    String accessTokenSecretKey = getTokenSecretKey(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY);

    return Jwts.builder()
        .setSubject(username)
        .claim(AUTHORITIES_KEY, authorities.stream().collect(joining(",")))
        .signWith(SignatureAlgorithm.HS512, accessTokenSecretKey)
        .setExpiration(new Date(now + accessTokenValidity))
        .compact();
  }

  private String createRefreshToken(String username, Set<String> authorities) {
    long now = (new Date()).getTime();
    long refreshTokenValidity = getTokenValidity(JWT_REFRESH_TOKEN_VALIDITY_PROPERTY);
    String refreshTokenSecretKey = getTokenSecretKey(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY);

    return Jwts.builder()
        .setSubject(username)
        .claim(AUTHORITIES_KEY, authorities.stream().collect(joining(",")))
        .signWith(SignatureAlgorithm.HS512, refreshTokenSecretKey)
        .setExpiration(new Date(now + refreshTokenValidity))
        .compact();
  }

  private long getTokenValidity(String propertyName) {
    return Long.valueOf(applicationPropertyService.findByName(propertyName).getPropertyValue());
  }

  private String getTokenSecretKey(String propertyName) {
    return applicationPropertyService.findByName(propertyName).getPropertyValue();
  }

  private void validateAccessToken(String token) {
    String accessTokenSecretKey = getTokenSecretKey(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY);

    Jwts.parser().setSigningKey(accessTokenSecretKey).parseClaimsJws(token);

  }

  private void validateRefreshToken(String token) {
    String refreshTokenSecretKey = getTokenSecretKey(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY);

    Jwts.parser().setSigningKey(refreshTokenSecretKey).parseClaimsJws(token);
  }

  private JwtCredential getAccessTokenCredential(String token) {
    String accessTokenSecretKey = getTokenSecretKey(JWT_ACCESS_TOKEN_SECRET_KEY_PROPERTY);

    return getCredential(token, accessTokenSecretKey);
  }

  private JwtCredential getRefreshTokenCredential(String token) {
    String refreshTokenSecretKey = getTokenSecretKey(JWT_REFRESH_TOKEN_SECRET_KEY_PROPERTY);

    return getCredential(token, refreshTokenSecretKey);
  }

  private JwtCredential getCredential(String token, String secretKey) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody();

    Set<String> authorities
        = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
        .collect(Collectors.toSet());

    return new JwtCredential(claims.getSubject(), authorities);
  }

}
