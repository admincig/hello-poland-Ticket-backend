package pl.hellopolandticket.service.dto;

import java.util.concurrent.atomic.AtomicInteger;
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
public class SightDTO {

  private Long id;
  private String name;
  private String lead;
  private String description;
  private String mainImageUrl;
  private String email;
  private String phone;
  private AtomicInteger availableTicketsNumber;
  private SightLocation sightLocation;

  public static SightDTO ofSightOnlyId(Sight sight) {
    return SightDTO.builder()
        .id(sight.getId())
        .build();
  }
}
