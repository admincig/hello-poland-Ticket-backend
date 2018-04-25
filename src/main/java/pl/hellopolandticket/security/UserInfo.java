package pl.hellopolandticket.security;

import static java.util.Arrays.asList;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

  private String name;
  private Set<String> roles = new HashSet<>();
  private String accessToken;
  private String refreshToken;

  public boolean hasRole(String _role) {
    return roles.contains(_role);
  }

  public boolean hasAnyRoles(String... roles) {
    return this.roles.stream().anyMatch(c -> asList(roles).contains(c));
  }

}
