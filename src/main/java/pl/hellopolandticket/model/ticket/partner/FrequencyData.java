package pl.hellopolandticket.model.ticket.partner;

import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

  @ElementCollection
  private List<Integer> daysOfWeek;

  @ElementCollection
  private List<Integer> daysOfMonth;

  @Column(name = "FREQUENCY")
  private Integer frequency;

  @Column(name = "FREQUENCY_START_DATE")
  private Date startDate;

  @Column(name = "FREQUENCY_END_DATE")
  private Date endDate;

  public FrequencyData(FrequencyType type, Integer frequency, Date startDate, Date endDate) {
    this.frequencyType = type;
    this.frequency = frequency;
    this.startDate = startDate;
    this.endDate = endDate;
  }

}
