package pl.hellopolandticket.model.ticket.market;

import static jakarta.persistence.CascadeType.ALL;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.ticket.market.Status.INVALID;
import static pl.hellopolandticket.model.util.UUIDGeneratorUtil.generateUUID;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Entity
@Table(name = "BOOKINGS")
@EqualsAndHashCode(exclude = {"tickets"})
@NoArgsConstructor
@ToString(exclude = "tickets")
public class Booking implements Serializable {

  private static final long serialVersionUID = 1536468158632140785L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "BOOKING_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "DATE", nullable = false)
  private Date date;

  @Setter
  @NotNull
  @Column(name = "CUSTOMER_NAME", nullable = false)
  private String customerName;

  @Setter
  @NotNull
  @Column(name = "CUSTOMER_EMAIL", nullable = false)
  private String customerEmail;

  @Setter
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private Status status = BOOKED;

  @Setter
  @OneToMany(cascade = ALL, orphanRemoval = true, mappedBy = "booking")
  private List<Ticket> tickets;

  @Setter
  @Column(name = "SERIAL_NUMBER", unique = true)
  private String serialNumber;

  @Setter
  @Column(name = "P24_ORDER_ID")
  private String P24OrderId;

  @Setter
  @Column(name = "P24_CURRENCY")
  private String p24Currency;

  @Setter
  @Column(name = "INVOICE")
  private Boolean invoice;

  @Setter
  @Column(name = "BUYER_NOTES", columnDefinition = "varchar")
  private String buyerNotes;

  @Deprecated
  @Setter
  @ElementCollection
  private Set<String> sightEventPdfAttachmentsPaths;

  @Builder
  public Booking(Date date, String customerName, String buyerNotes, String customerEmail,
      Boolean invoice, Set<String> sightEventPdfAttachmentsPaths) {
    this.date = date;
    this.buyerNotes = buyerNotes;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
    this.invoice = invoice;
    this.sightEventPdfAttachmentsPaths = sightEventPdfAttachmentsPaths;
    this.serialNumber = generateUUID();
  }

  public void makeInvalid() {
    setStatus(INVALID);
    tickets.forEach(this::setTicketStatusesAsInvalidAndIncreaseAvailableTicketsNumber);
  }

  private void setTicketStatusesAsInvalidAndIncreaseAvailableTicketsNumber(Ticket ticket) {
    ticket.setStatus(INVALID);
    ticket.getTicketPool().increaseAvailableTicketsNumber();
  }

}
