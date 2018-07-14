package pl.hellopolandticket.model.ticket.partner;

import static javax.persistence.CascadeType.ALL;
import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import lombok.ToString;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.preconditionfailed.NumberOfTicketsNotPositiveException;

@Getter
@Entity
@Table(name = "TICKET_POOLS")
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class TicketPool implements Serializable {

  private static final long serialVersionUID = -3301750425362262797L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_POOL_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @NotNull
  @Column(name = "AVAILABLE_TICKETS_NUMBER", nullable = false)
  private Integer availableTicketsNumber;

  @Setter
  @Column(name = "START_DATE")
  private Date startDate;

  @Setter
  @Column(name = "END_DATE")
  private Date endDate;

  @Setter
  @NotNull
  @Column(name = "PREDEFINED_DATE", nullable = false)
  private Boolean predefinedDate;

  @Setter
  @Column(name = "DATE")
  private Date date;

  @Setter
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "DATE_TYPE", nullable = false)
  private DateType dateType;

  @Setter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "SIGHT_EVENT_ID", nullable = false)
  private SightEvent sightEvent;

  @Setter
  @OneToMany(cascade = ALL, orphanRemoval = true, mappedBy = "ticketPool")
  private List<TicketDefinition> ticketDefinitions = new ArrayList<>();

  @Builder
  public TicketPool(String name, Integer availableTicketsNumber, Date startDate, Date endDate,
      Boolean predefinedDate, Date date, DateType dateType, SightEvent sightEvent,
      List<TicketDefinition> ticketDefinitions) {
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.predefinedDate = predefinedDate;
    this.date = date;
    this.dateType = dateType;
    this.sightEvent = sightEvent;
    this.ticketDefinitions = ticketDefinitions;

    this.availableTicketsNumber =
        availableTicketsNumber == null ? UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE
            : availableTicketsNumber;
  }

  public void decreaseAvailableTicketsNumber(int numberOfTickets) {
    if (numberOfTickets <= 0) {
      throw new NumberOfTicketsNotPositiveException();
    }

    if (hasLimitedNumberOfTickets()) {
      if (hasEnoughTickets(numberOfTickets)) {
        availableTicketsNumber = availableTicketsNumber - numberOfTickets;
      } else {
        throw new NoAvailableTicketsException();
      }
    }
  }

  public void increaseAvailableTicketsNumber() {
    if (hasLimitedNumberOfTickets()) {
      availableTicketsNumber++;
    }
  }

  private boolean hasLimitedNumberOfTickets() {
    return availableTicketsNumber != UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }

  private boolean hasEnoughTickets(int numberOfTickets) {
    return availableTicketsNumber - numberOfTickets >= MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }
}
