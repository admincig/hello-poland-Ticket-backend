package pl.hellopolandticket.model;

import static java.util.UUID.randomUUID;
import static javax.persistence.FetchType.EAGER;
import static pl.hellopolandticket.model.TicketStatus.BOOKED;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "TICKETS")
@EqualsAndHashCode
@NoArgsConstructor
public class Ticket implements Serializable {

  private static final long serialVersionUID = 8362327972408128723L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_ID")
  private Long id;

  @Setter
  @NotNull
  @ManyToOne(optional = false, fetch = EAGER)
  @JoinColumn(name = "SIGHT_ID", nullable = false)
  private Sight sight;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @NotNull
  @Column(name = "PRICE", nullable = false)
  private Integer price;

  @Setter
  @Column(name = "DATE")
  private Date date;

  @Setter
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "TICKET_STATUS", nullable = false)
  private TicketStatus ticketStatus = BOOKED;

  @Setter
  @NotNull
  @Column(name = "SERIAL_NUMBER", nullable = false)
  private Long serialNumber;

  @Builder
  public Ticket(Sight sight, String name, Integer price, Date date, TicketStatus ticketStatus) {
    this.sight = sight;
    this.name = name;
    this.price = price;
    this.date = date;
    this.ticketStatus = ticketStatus;
    this.serialNumber = randomUUID().getMostSignificantBits();
  }
}