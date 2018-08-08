package pl.hellopolandticket.model.ticket.partner;

import static javax.persistence.CascadeType.ALL;
import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
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
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.preconditionfailed.NumberOfTicketsNotPositiveException;

@Getter
@Entity
@Table(name = "TICKET_DEFINITIONS")
@EqualsAndHashCode(exclude = {"ticketPoolDefinitions", "tickets"})
@NoArgsConstructor
@ToString(exclude = {"ticketPoolDefinitions", "tickets"})
public class TicketDefinition implements Serializable {

  private static final long serialVersionUID = -8863063758760873368L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_DEFINITION_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @NotNull
  @Column(name = "PRICE", nullable = false)
  private Integer price;

  @Setter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "PARTNER_ID", nullable = false)
  private Partner partner;

  @Setter
  @Column(name = "AVAILABLE_TICKETS_NUMBER")
  private Integer availableTicketsNumber;

  @Setter
  @ManyToMany(cascade = ALL)
  private List<TicketPoolDefinition> ticketPoolDefinitions;

  @Setter
  @OneToMany(cascade = ALL, orphanRemoval = true, mappedBy = "ticketDefinition")
  private List<Ticket> tickets;

  @Builder
  public TicketDefinition(String name, Integer availableTicketsNumber, Integer price,
      Partner partner, List<TicketPoolDefinition> ticketPoolDefinitions) {
    this.name = name;
    this.price = price;
    this.partner = partner;
    this.ticketPoolDefinitions = ticketPoolDefinitions;

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
