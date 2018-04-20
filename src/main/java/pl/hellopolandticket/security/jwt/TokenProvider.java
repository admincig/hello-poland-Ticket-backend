package pl.hellopolandticket.security.jwt;

import static java.util.stream.Collectors.joining;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class TokenProvider {

  private static final String AUTHORITIES_KEY = "auth";

  private static final String SECRET_KEY_PROPERTY = "jwt.secretKey";

  private static final String TOKEN_VALIDITY_PROPERTY = "jwt.tokenValidityMillis";

  private String secretKey;

  private long tokenValidity;

  @PostConstruct
  public void init() {
    secretKey = System.getProperty(SECRET_KEY_PROPERTY);
    tokenValidity = Long.valueOf(System.getProperty(TOKEN_VALIDITY_PROPERTY));
  }

  public String createToken(String username, Set<String> authorities) {
    long now = (new Date()).getTime();

    return Jwts.builder()
        .setSubject(username)
        .claim(AUTHORITIES_KEY, authorities.stream().collect(joining(",")))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setExpiration(new Date(now + tokenValidity))
        .compact();
  }

  public JwtCredential getCredential(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody();

    Set<String> authorities
        = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
        .collect(Collectors.toSet());

    return new JwtCredential(claims.getSubject(), authorities);
  }

  public void validateToken(String authToken) {
    Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
  }
}
