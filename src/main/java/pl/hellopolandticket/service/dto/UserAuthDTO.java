package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.security.CurrentUser;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthDTO implements Serializable {

  private static final long serialVersionUID = 1790587694120511896L;

  private String email;

  private String accessToken;

  private String refreshToken;

  public static UserAuthDTO ofCurrentUser(CurrentUser currentUser) {
    return UserAuthDTO.builder()
        .email(currentUser.getEmail())
        .accessToken(currentUser.getAccessToken())
        .refreshToken(currentUser.getRefreshToken())
        .build();
  }
}
