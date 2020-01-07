package pl.hellopolandticket.model.util;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.partner.Limited;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Getter
@Entity
@Table(name = "AVAILABLE_TICKET_NUMBER_ASSOCIATION", uniqueConstraints = @UniqueConstraint(
    columnNames = {"TICKET_DEFINITION_ID", "TICKET_POOL_DEFINITION_ID", "TICKET_POOL_ID"}))
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = {"parent", "children"})
public class AvailableTicketNumberAssociation implements Serializable, Limited {
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
  @ManyToOne(optional = false)
  @JoinColumn(name = "TICKET_DEFINITION_ID", nullable = false)
  private TicketDefinition ticketDefinition;

  @Setter
  @ManyToOne
  @JoinColumn(name = "TICKET_POOL_DEFINITION_ID")
  private TicketPoolDefinition ticketPoolDefinition;

  @Setter
  @ManyToOne
  @JoinColumn(name = "TICKET_POOL_ID")
  private TicketPool ticketPool;

  @Setter
  @ManyToOne
  @JoinColumn(name = "PARENT_ID")
  private AvailableTicketNumberAssociation parent;

  @Setter
  private boolean deleted;

  @Setter
  @OneToMany(mappedBy = "parent")
  private List<AvailableTicketNumberAssociation> children;

  @Embedded
  @Setter
  private Discount discount;

  @Builder
  public AvailableTicketNumberAssociation(@NotNull Integer availableTicketsNumber,
      @NotNull TicketDefinition ticketDefinition, TicketPoolDefinition ticketPoolDefinition,
      TicketPool ticketPool, AvailableTicketNumberAssociation parent) {
    this.parent = parent;
    this.availableTicketsNumber = availableTicketsNumber;
    this.ticketDefinition = ticketDefinition;
    this.ticketPoolDefinition = ticketPoolDefinition;
    this.ticketPool = ticketPool;
  }


  public int getBoughtTicketsCount() {
    return (int) ticketDefinition.getTickets().stream()
        .filter(t -> Status.BOUGHT.equals(t.getStatus()))
        .count();
  }

  @Override
  public int getTicketsLeftToBuyCount() {
    if (parent == null) {
      return availableTicketsNumber;
    }
    if (parent.getAvailableTicketsNumber() == -1) {
      return -1;
    }
    return Math.max(0, parent.getAvailableTicketsNumber() - getBoughtTicketsCount());
  }

}
