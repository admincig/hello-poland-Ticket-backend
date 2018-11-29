package pl.hellopolandticket.model.partner;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
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
  @Column(name = "EMAIL")
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
