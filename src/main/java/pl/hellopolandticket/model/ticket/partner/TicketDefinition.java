package pl.hellopolandticket.model.ticket.partner;

import static jakarta.persistence.CascadeType.ALL;
import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
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
@EqualsAndHashCode(exclude = {"atnas", "tickets", "interestingAtna"})
@NoArgsConstructor
@ToString(exclude = {"atnas", "tickets", "interestingAtna"})
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

  private boolean deleted;

  @Transient
  private AvailableTicketNumberAssociation interestingAtna;

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

  public List<AvailableTicketNumberAssociation> getAtnasConnectedToPoolDefinitions() {
    return this.atnas.stream()
        .filter(atna -> atna.getTicketPoolDefinition() != null)
        .collect(Collectors.toList());
  }

  public AvailableTicketNumberAssociation getAtna(TicketPool ticketPool) {
    return this.atnas.stream()
        .filter(atna -> atna.getTicketPool() != null)
        .filter(atna -> atna.getTicketPool().getId().equals(ticketPool.getId()))
        .findFirst()
        .get();
  }

  @Transient
  private transient Logger logger = System.getLogger("TicketDefinition-" + id);

  public AvailableTicketNumberAssociation getAtna(TicketPoolDefinition tpd) {
    var list = this.atnas.stream()
        .filter(atna -> atna.getTicketPoolDefinition() != null)
        .filter(atna -> atna.getTicketPoolDefinition().getId().equals(tpd.getId()))
        .collect(Collectors.toList());
    if (list.size() > 1) {
      Collections.sort(list, Comparator.comparing(AvailableTicketNumberAssociation::isDeleted));
      logger.log(Level.DEBUG,
          "Found more than one matching ATNA, sorting by isDeleted and returning first");
    }
    return list.get(0);
  }

}
