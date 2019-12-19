package pl.hellopolandticket.model.ticket.partner;

import static javax.persistence.CascadeType.ALL;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
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
import lombok.ToString;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

@Getter
@Setter
@Entity
@Table(name = "TICKET_DEFINITIONS")
@EqualsAndHashCode(exclude = {"atnas", "tickets"})
@NoArgsConstructor
@ToString(exclude = {"atnas", "tickets"})
public class TicketDefinition implements Serializable {

  private static final long serialVersionUID = -8863063758760873368L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_DEFINITION_ID")
  private Long id;

  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @NotNull
  @Column(name = "PRICE", nullable = false)
  private Integer price;

  @NotNull
  @ManyToOne
  @JoinColumn(name = "PARTNER_ID", nullable = false)
  private Partner partner;

  @OneToMany(mappedBy = "ticketDefinition")
  private List<AvailableTicketNumberAssociation> atnas;

  @OneToMany(cascade = ALL, orphanRemoval = true, mappedBy = "ticketDefinition")
  private List<Ticket> tickets;

  @Builder
  public TicketDefinition(String name, Integer price, Partner partner,
      List<TicketPoolDefinition> ticketPoolDefinitions) {
    this.name = name;
    this.price = price;
    this.partner = partner;
  }

  public boolean isConnectedWithPoolDefiniton(Long poolDefinitionId) {
    return atnas.stream()
        .anyMatch(atna -> atna.getTicketPoolDefinition() != null
            && Objects.equals(poolDefinitionId, atna.getTicketPoolDefinition().getId()));
  }

  public AvailableTicketNumberAssociation getAtna(TicketPool ticketPool) {
    return this.atnas.stream()
        .filter(atna -> atna.getTicketPool().getId().equals(ticketPool.getId()))
        .findFirst()
        .get();
  }



}
