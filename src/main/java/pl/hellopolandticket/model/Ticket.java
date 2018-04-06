package pl.hellopolandticket.model;

import static java.util.UUID.randomUUID;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static pl.hellopolandticket.model.Status.BOOKED;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
  @ManyToOne(optional = false, fetch = LAZY)
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
  @NotNull
  @Column(name = "DATE", nullable = false)
  private Date date;

  @Setter
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private Status status = BOOKED;

  @Setter
  @Column(name = "SERIAL_NUMBER", unique = true)
  private String serialNumber;

  @Setter
  @NotNull
  @ManyToOne(cascade = ALL)
  @JoinColumn(name = "BOOKING", nullable = false)
  private Booking booking;

  @Setter
  @NotNull
  @ManyToOne(cascade = ALL, optional = false)
  @JoinColumn(name = "TICKET_DEFINITION", nullable = false)
  private TicketDefinition ticketDefinition;

  @Builder
  public Ticket(Sight sight, String name, Integer price, Date date, Status status,
      String serialNumber, Booking booking, TicketDefinition ticketDefinition) {
    this.sight = sight;
    this.name = name;
    this.price = price;
    this.date = date;
    this.status = status;
    this.serialNumber = serialNumber;
    this.booking = booking;
    this.ticketDefinition = ticketDefinition;
  }

  public void generateSerialNumber() {
    try {
      MessageDigest salt = MessageDigest.getInstance("SHA-256");
      salt.update(randomUUID().toString().getBytes("UTF-8"));
      serialNumber = printHexBinary(salt.digest());
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Can't generate random UUID {}", e);
    }
  }
}