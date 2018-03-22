package pl.hellopolandticket.model;

import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "TICKET_DEFINITIONS")
@EqualsAndHashCode
@NoArgsConstructor
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
  @Column(name = "PREDEFINED_DATE", nullable = false)
  private Boolean predefinedDate;

  @Setter
  @Column(name = "DATE")
  private Date date;

  @Setter
  @NotNull
  @ManyToOne(optional = false, fetch = EAGER)
  @JoinColumn(name = "SIGHT_ID", nullable = false)
  private Sight sight;

  @Builder
  public TicketDefinition(String name, Integer price, Boolean predefinedDate, Date date,
      Sight sight) {
    this.name = name;
    this.price = price;
    this.predefinedDate = predefinedDate;
    this.date = date;
    this.sight = sight;
  }

}