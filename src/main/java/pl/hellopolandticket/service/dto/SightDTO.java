package pl.hellopolandticket.service.dto;

import java.util.Date;
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
  private Date date;
  private String lead;
  private String description;
  private String mainImageUrl;
  private String email;
  private String phone;
  private Integer availableTicketsNumber;
  private SightLocation sightLocation;

  private Integer totalTicketsNumber;
  private Integer punchedTicketsNumber;

  public static SightDTO ofSightOnlyId(Sight sight) {
    return SightDTO.builder()
        .id(sight.getId())
        .build();
  }

  public static SightDTO ofSightWithValidAndPunchTickets(Sight sight, int validTicketsNumber,
      int punchedTicketsNumber) {
    return SightDTO.builder()
        .id(sight.getId())
        .name(sight.getName())
        .date(sight.getDate())
        .totalTicketsNumber(validTicketsNumber)
        .punchedTicketsNumber(punchedTicketsNumber)
        .build();

  }
}
