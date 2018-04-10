package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.Date;
import javax.json.bind.annotation.JsonbDateFormat;
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

  @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private Date date;

  private String lead;

  private String description;

  private String mainImageUrl;

  private String email;

  private String phone;

  private Integer availableTicketsNumber;

  private SightLocation sightLocation;


  private Integer boughtTicketNumber;

  private Integer totalTicketsNumber;

  public static SightDTO ofSightOnlyId(Sight sight) {
    return SightDTO.builder()
        .id(sight.getId())
        .build();
  }

  public static SightDTO ofSightWithBoughtAndTotalTickets(Sight sight, int boughtTicketNumber,
      int totalTicketsNumber) {
    return SightDTO.builder()
        .id(sight.getId())
        .name(sight.getName())
        .date(sight.getDate())
        .boughtTicketNumber(boughtTicketNumber)
        .totalTicketsNumber(totalTicketsNumber)
        .build();

  }
}
