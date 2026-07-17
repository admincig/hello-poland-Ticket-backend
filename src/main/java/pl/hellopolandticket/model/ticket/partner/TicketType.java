package pl.hellopolandticket.model.ticket.partner;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "TICKET_TYPES")
@NoArgsConstructor
@ToString(exclude = "ticketDefinitions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TicketType implements Serializable {

  public static final String NORMALNY_CODE = "NORMALNY";
  public static final String ULGOWY_CODE = "ULGOWY";
  public static final String ULGOWY_STUDENT_UCZEN_CODE = "ULGOWY_STUDENT_UCZEN";
  public static final String SPECJALNY_CODE = "SPECJALNY";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_TYPE_ID")
  @EqualsAndHashCode.Include
  private Long id;

  @NotNull
  @Column(name = "CODE", nullable = false, unique = true)
  private String code;

  @NotNull
  @Column(name = "LABEL", nullable = false)
  private String label;

  @NotNull
  @Column(name = "ELIGIBLE_FOR_PRICE_FROM", nullable = false)
  private boolean eligibleForPriceFrom;

  @NotNull
  @Column(name = "ACTIVE", nullable = false)
  private boolean active;

  @NotNull
  @Column(name = "SORT_ORDER", nullable = false)
  private Integer sortOrder;

  @OneToMany(mappedBy = "ticketType")
  private List<TicketDefinition> ticketDefinitions;

}
