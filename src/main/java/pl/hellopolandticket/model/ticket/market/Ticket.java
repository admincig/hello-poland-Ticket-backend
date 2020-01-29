package pl.hellopolandticket.model.ticket.market;

import static javax.persistence.CascadeType.PERSIST;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.util.Discount;
import pl.hellopolandticket.service.exception.badrequest.CannotGenerateQrCodeException;

@Getter
@Setter
@Entity
@Table(name = "TICKETS")
@EqualsAndHashCode
@NoArgsConstructor
@ToString(exclude = {"ticketPool"})
public class Ticket implements Serializable {

  private static final long serialVersionUID = 8362327972408128723L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_ID")
  private Long id;

  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @NotNull
  @Column(name = "PRICE", nullable = false)
  private Integer price;

  @NotNull
  @Column(name = "DATE", nullable = false)
  private Date date;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false)
  private Status status = BOOKED;

  @Column(name = "SERIAL_NUMBER", unique = true)
  private String serialNumber;

  @NotNull
  @ManyToOne(cascade = PERSIST)
  @JoinColumn(name = "BOOKING", nullable = false)
  private Booking booking;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "TICKET_DEFINITION_ID", nullable = false)
  private TicketDefinition ticketDefinition;

  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "TICKET_POOL_ID", nullable = false)
  private TicketPool ticketPool;

  @ManyToOne
  @JoinColumn(name = "TICKET_TAKER_ID")
  private User ticketTaker;

  @Column(name = "PUNCHING_DATE_ID")
  private Date punchingDate;

  @Embedded
  private Discount discount;


  @Builder
  public Ticket(String name, Integer price, Date date, Status status, String serialNumber,
      Booking booking, TicketPool ticketPool, TicketDefinition ticketDefinition,
      Discount discount) {
    this.name = name;
    this.price = price;
    this.date = date;
    this.status = status;
    this.serialNumber = serialNumber;
    this.booking = booking;
    this.ticketPool = ticketPool;
    this.ticketDefinition = ticketDefinition;
    this.discount = discount;
  }

  public ByteArrayOutputStream encodeSerialNumberAsQrCode(int qrCodeWidth, int qrCodeHeight) {
    try {
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix =
          qrCodeWriter.encode(getSerialNumber(), BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight);

      ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

      return pngOutputStream;
    } catch (WriterException | IOException e) {
      throw new CannotGenerateQrCodeException();
    }
  }
}
