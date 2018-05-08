package pl.hellopolandticket.security.jwt;

import java.util.Set;
import javax.security.enterprise.credential.Credential;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class JwtCredential implements Credential {

  private final String principal;
  private final Set<String> authorities;

  public String getPrincipal() {
    return principal;
  }

  public Set<String> getAuthorities() {
    return authorities;
  }

}
