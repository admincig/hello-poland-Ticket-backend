package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.SightLocation;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SightDTO implements Serializable {

  private static final long serialVersionUID = -585533388870461406L;

  private Long id;

  private String name;

  private String lead;

  private String description;

  private String mainImageUrl;

  private String email;

  private String phone;

  private SightLocation sightLocation;


  public static SightDTO ofSightBasic(Sight sight) {
    return SightDTO.builder()
        .id(sight.getId())
        .name(sight.getName())
        .build();
  }


}
