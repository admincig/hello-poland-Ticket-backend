package pl.hellopolandticket.model.partner;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;

@Getter
@Entity
@Table(name = "PARTNERS")
@EqualsAndHashCode(exclude = {"sightEvents", "users", "ticketDefinitions"})
@NoArgsConstructor
@ToString(exclude = {"sightEvents", "users", "ticketDefinitions"})
public class Partner implements Serializable {

  private static final long serialVersionUID = 6118414827783500940L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "PARTNER_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false, unique = true)
  private String name;

  @Setter
  @NotNull
  @Column(name = "EMAIL", nullable = false, unique = true)
  private String email;

  @Setter
  @OneToMany(mappedBy = "partner")
  private List<User> users;

  @Setter
  @OneToMany(mappedBy = "partner")
  private List<SightEvent> sightEvents;

  @Setter
  @OneToMany(mappedBy = "partner")
  private List<TicketDefinition> ticketDefinitions;

  @Builder
  public Partner(String name, String email) {
    this.name = name;
    this.email = email;
  }

}
