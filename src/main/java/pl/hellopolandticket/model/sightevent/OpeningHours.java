package pl.hellopolandticket.model.sightevent;

import java.io.Serializable;
import java.time.LocalTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "OPENING_HOURS")
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class OpeningHours implements Serializable {

  private static final long serialVersionUID = 1324999000156403842L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "OPENING_HOURS_ID")
  private Long id;

  @Setter
  @ManyToOne(optional = true)
  private SightEvent sightEvent;

  @Setter
  @NotNull
  private Integer day;

  @Setter
  @NotNull
  private LocalTime openTime;

  @Setter
  @NotNull
  private LocalTime closeTime;

  @Builder
  public OpeningHours(SightEvent sightEvent, @NotNull Integer day, @NotNull LocalTime openTime,
      @NotNull LocalTime closeTime) {
    this.sightEvent = sightEvent;
    this.day = day;
    this.openTime = openTime;
    this.closeTime = closeTime;
  }

}
