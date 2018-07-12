package pl.hellopolandticket.model.ticket.partner;

import java.time.DayOfWeek;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FrequencyData {

  @Enumerated(EnumType.STRING)
  @Column(name = "FREQUENCY_TYPE")
  private FrequencyType frequencyType;

  @Enumerated(EnumType.STRING)
  @Column(name = "DAY_OF_WEEK")
  private DayOfWeek dayOfWeek;

  @Column(name = "DAY_OF_MONTH")
  private Integer dayOfMonth;

  @Column(name = "MONTH")
  private Integer month;

  @Column(name = "FREQUENCY")
  private Integer frequency;
}
