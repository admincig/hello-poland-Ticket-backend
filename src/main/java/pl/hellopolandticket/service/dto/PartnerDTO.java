package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.User;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"users", "sightEvents", "token"})
public class PartnerDTO implements Serializable {

  private static final long serialVersionUID = 4969716130327189424L;

  private Long id;
  private String name;
  private List<User> users;
  private List<SightEventDTO> sightEvents;
  private String token;

  public static PartnerDTO ofPartnerWithToken(Partner partner, String token) {
    return PartnerDTO.builder()
        .id(partner.getId())
        .name(partner.getName())
        .token(token)
        .build();
  }
}
