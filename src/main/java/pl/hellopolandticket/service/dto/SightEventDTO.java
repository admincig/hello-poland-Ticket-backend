package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.Date;
import javax.json.bind.annotation.JsonbDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.SightEvent;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SightEventDTO implements Serializable {

  private static final long serialVersionUID = 198722041860707628L;

  private Long id;

  private String name;

  @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private Date date;

  private Integer availableTicketsNumber;

  private String description;

  private Integer duration;

  private Integer boughtTicketNumber;

  private Integer totalTicketsNumber;

  public static SightEventDTO ofSightEventBasic(SightEvent sightEvent) {
    return SightEventDTO.builder()
        .id(sightEvent.getId())
        .name(sightEvent.getName())
        .date(sightEvent.getDate())
        .build();
  }

  public static SightEventDTO ofSightEventWithBoughtAndTotalTickets(SightEvent sightEvent,
      int boughtTicketNumber,
      int totalTicketsNumber) {
    return SightEventDTO.builder()
        .id(sightEvent.getId())
        .name(sightEvent.getName())
        .boughtTicketNumber(boughtTicketNumber)
        .totalTicketsNumber(totalTicketsNumber)
        .build();

  }
}
