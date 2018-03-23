package pl.hellopolandticket.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.converter.AtomicIntegerToIntegerConverter;
import pl.hellopolandticket.service.exception.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.NumberOfTicketsNotPositiveException;

@Getter
@Entity
@Table(name = "SIGHTS")
@EqualsAndHashCode
@NoArgsConstructor
public class Sight implements Serializable {

  private static final long serialVersionUID = -8863063758760873368L;

  private static final int UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE = -1;

  private static final int MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "SIGHT_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false, unique = true)
  private String name;

  @Setter
  @Column(name = "LEAD")
  private String lead;

  @Setter
  @Column(name = "DESCRIPTION")
  private String description;

  @Setter
  @Column(name = "MAIN_IMAGE_URL")
  private String mainImageUrl;

  @Setter
  @Column(name = "EMAIL")
  private String email;

  @Setter
  @Column(name = "PHONE")
  private String phone;

  @Setter
  @Column(name = "AVAILABLE_TICKETS_NUMBER")
  @Convert(converter = AtomicIntegerToIntegerConverter.class)
  private AtomicInteger availableTicketsNumber;

  @Setter
  @Embedded
  private SightLocation sightLocation;

  @Builder
  public Sight(String name, String lead, String description, String mainImageUrl, String email,
      String phone, Integer availableTicketsNumber, SightLocation sightLocation) {
    this.name = name;
    this.lead = lead;
    this.description = description;
    this.mainImageUrl = mainImageUrl;
    this.email = email;
    this.phone = phone;
    this.sightLocation = sightLocation;

    this.availableTicketsNumber =
        availableTicketsNumber == null ?
            new AtomicInteger(UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE)
            : new AtomicInteger(availableTicketsNumber);
  }

  public void decreaseAvailableTicketsNumber(int numberOfTickets) {
    if (numberOfTickets <= 0) {
      throw new NumberOfTicketsNotPositiveException();
    }

    if (!hasUnlimitedNumberOfTickets()) {
      if (hasEnoughTickets(numberOfTickets)) {
        availableTicketsNumber.set(availableTicketsNumber.get() - numberOfTickets);
      } else {
        throw new NoAvailableTicketsException();
      }
    }
  }

  private boolean hasUnlimitedNumberOfTickets() {
    return availableTicketsNumber.get() == UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }

  private boolean hasEnoughTickets(int numberOfTickets) {
    return availableTicketsNumber.get() - numberOfTickets >= MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }
}