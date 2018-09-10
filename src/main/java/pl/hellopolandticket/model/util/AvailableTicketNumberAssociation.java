package pl.hellopolandticket.model.util;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Getter
@Entity
@Table(name = "AVAILABLE_TICKET_NUMBER_ASSOCIATION", uniqueConstraints = @UniqueConstraint(
    columnNames = {"ticketDefinition", "ticketPoolDefinition", "ticketPool"}))
@NoArgsConstructor
@EqualsAndHashCode
@ToString()
public class AvailableTicketNumberAssociation implements Serializable {
  private static final long serialVersionUID = -399400359685830420L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @Column(name = "AVAILABLE_TICKET_NUMBER_ASSOCIATION_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "AVAILABLE_TICKETS_NUMBER", nullable = false)
  private Integer availableTicketsNumber;

  @Setter
  @NotNull
  @Column(name = "TICKET_DEFINITION", nullable = false)
  private TicketDefinition ticketDefinition;

  @Setter
  @Column(name = "TICKET_POOL_DEFINITION")
  private TicketPoolDefinition ticketPoolDefinition;

  @Setter
  @Column(name = "TICKET_POOL")
  private TicketPool ticketPool;

  @Builder
  public AvailableTicketNumberAssociation(@NotNull Integer availableTicketsNumber,
      @NotNull TicketDefinition ticketDefinition, TicketPoolDefinition ticketPoolDefinition,
      TicketPool ticketPool) {
    this.availableTicketsNumber = availableTicketsNumber;
    this.ticketDefinition = ticketDefinition;
    this.ticketPoolDefinition = ticketPoolDefinition;
    this.ticketPool = ticketPool;
  }

}
