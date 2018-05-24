package pl.hellopolandticket.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.preconditionfailed.NumberOfTicketsNotPositiveException;

@Getter
@Entity
@Table(name = "SIGHT_EVENTS")
@EqualsAndHashCode
@NoArgsConstructor
public class SightEvent implements Serializable {

  private static final long serialVersionUID = 5345966403908441388L;

  public static final int UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE = -1;

  private static final int MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "SIGHT_EVENT_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @Column(name = "DATE")
  private Date date;

  @Setter
  @Column(name = "AVAILABLE_TICKETS_NUMBER")
  private Integer availableTicketsNumber;

  @Setter
  @Column(name = "DESCRIPTION")
  private String description;

  @Setter
  @Column(name = "DURATION")
  private Integer duration;

  @Setter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "SIGHT_ID", nullable = false)
  private Sight sight;

  @Setter
  @OneToMany(mappedBy = "sightEvent")
  private List<TicketDefinition> ticketDefinitions;

  @Builder
  public SightEvent(String name, Date date, Integer availableTicketsNumber, String description,
      Integer duration, Sight sight) {
    this.name = name;
    this.date = date;
    this.description = description;
    this.duration = duration;
    this.sight = sight;

    this.availableTicketsNumber =
        availableTicketsNumber == null ? UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE
            : availableTicketsNumber;
  }


  public void decreaseAvailableTicketsNumber(int numberOfTickets) {
    if (numberOfTickets <= 0) {
      throw new NumberOfTicketsNotPositiveException();
    }

    if (!hasUnlimitedNumberOfTickets()) {
      if (hasEnoughTickets(numberOfTickets)) {
        availableTicketsNumber = availableTicketsNumber - numberOfTickets;
      } else {
        throw new NoAvailableTicketsException();
      }
    }
  }

  public void increaseAvailableTicketsNumber() {
    if (!hasUnlimitedNumberOfTickets()) {
      availableTicketsNumber++;
    }
  }

  private boolean hasUnlimitedNumberOfTickets() {
    return availableTicketsNumber == UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }

  private boolean hasEnoughTickets(int numberOfTickets) {
    return availableTicketsNumber - numberOfTickets >= MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }
}
