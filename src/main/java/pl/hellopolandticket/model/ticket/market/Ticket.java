package pl.hellopolandticket.model.ticket.market;

import static javax.persistence.CascadeType.PERSIST;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.util.UUIDGeneratorUtil.generateUUID;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.ticket.partner.DateType;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.service.exception.badrequest.CannotGenerateQrCodeException;

@Slf4j
@Getter
@Entity
@Table(name = "TICKETS")
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class Ticket implements Serializable {

  private static final long serialVersionUID = 8362327972408128723L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_ID")
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
  @Column(name = "DATE", nullable = false)
  private Date date;

  @Setter
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "DATE_TYPE", nullable = false)
  private DateType dateType;

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
  @ManyToOne(cascade = PERSIST)
  @JoinColumn(name = "BOOKING", nullable = false)
  private Booking booking;

  @Setter
  @NotNull
  @ManyToOne(optional = false)
  @JoinColumn(name = "TICKET_DEFINITION", nullable = false)
  private TicketDefinition ticketDefinition;

  @Setter
  @ManyToOne
  @JoinColumn(name = "TICKET_TAKER")
  private User ticketTaker;

  @Setter
  @Column(name = "PUNCHING_DATE")
  private Date punchingDate;

  @Builder
  public Ticket(String name, Integer price, Date date, DateType dateType, Status status,
      String serialNumber, Booking booking, TicketDefinition ticketDefinition) {
    this.name = name;
    this.price = price;
    this.date = date;
    this.dateType = dateType;
    this.status = status;
    this.serialNumber = serialNumber;
    this.booking = booking;
    this.ticketDefinition = ticketDefinition;
  }

  public void generateSerialNumber() {
    serialNumber = generateUUID();
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
