package pl.hellopolandticket.model.util;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
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
@Setter
@Entity
@Table(name = "AVAILABLE_TICKET_NUMBER_ASSOCIATION", uniqueConstraints = @UniqueConstraint(
    columnNames = {"TICKET_DEFINITION_ID", "TICKET_POOL_DEFINITION_ID", "TICKET_POOL_ID"}))
@NoArgsConstructor
@EqualsAndHashCode
@ToString(
    exclude = {"parent", "children", "ticketDefinition", "ticketPool", "ticketPoolDefinition"})
public class AvailableTicketNumberAssociation implements Serializable, Limited {
  private static final long serialVersionUID = -399400359685830420L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // @Column(name = "AVAILABLE_TICKET_NUMBER_ASSOCIATION_ID")
  private Long id;

  @NotNull
  @Column(name = "AVAILABLE_TICKETS_NUMBER", nullable = false)
  private Integer availableTicketsNumber;

  @NotNull
  @ManyToOne(optional = false, cascade = CascadeType.REFRESH)
  @JoinColumn(name = "TICKET_DEFINITION_ID", nullable = false)
  private TicketDefinition ticketDefinition;

  @ManyToOne
  @JoinColumn(name = "TICKET_POOL_DEFINITION_ID")
  private TicketPoolDefinition ticketPoolDefinition;

  @ManyToOne
  @JoinColumn(name = "TICKET_POOL_ID")
  private TicketPool ticketPool;

  @ManyToOne
  @JoinColumn(name = "PARENT_ID")
  private AvailableTicketNumberAssociation parent;

  private boolean deleted;

  @OneToMany(mappedBy = "parent")
  private List<AvailableTicketNumberAssociation> children;

  @Embedded
  private Discount discount;

  @Builder
  public AvailableTicketNumberAssociation(@NotNull Integer availableTicketsNumber,
      @NotNull TicketDefinition ticketDefinition, TicketPoolDefinition ticketPoolDefinition,
      TicketPool ticketPool, AvailableTicketNumberAssociation parent, Discount discount) {
    this.parent = parent;
    this.availableTicketsNumber = availableTicketsNumber;
    this.ticketDefinition = ticketDefinition;
    this.ticketPoolDefinition = ticketPoolDefinition;
    this.ticketPool = ticketPool;
    this.discount = discount;
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

    public Discount getEffectiveDiscount() {
        if (discount != null) {
            return discount;
        }
        if (parent != null) {
            return parent.getEffectiveDiscount();
        }
        return null;
    }


}
