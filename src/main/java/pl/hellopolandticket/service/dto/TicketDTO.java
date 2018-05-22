package pl.hellopolandticket.service.dto;

import static pl.hellopolandticket.service.dto.BookingDTO.ofBookingBasic;
import static pl.hellopolandticket.service.dto.SightEventDTO.ofSightEventBasic;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;
import javax.json.bind.annotation.JsonbDateFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Status;
import pl.hellopolandticket.model.Ticket;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO implements Serializable {

  private static final long serialVersionUID = -3362200913434743101L;

  private Long id;

  private SightEventDTO sightEvent;

  private String name;

  private Integer price;

  @JsonbDateFormat(value = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private Date date;

  private Status status;

  private String serialNumber;

  private ByteArrayOutputStream qrCode;

  private BookingDTO booking;

  public static TicketDTO ofTicket(Ticket ticket) {
    return TicketDTO.builder()
        .id(ticket.getId())
        .sightEvent(ofSightEventBasic((ticket.getSightEvent())))
        .name(ticket.getName())
        .price(ticket.getPrice())
        .date(ticket.getDate())
        .status(ticket.getStatus())
        .serialNumber(ticket.getSerialNumber())
        .booking(ofBookingBasic(ticket.getBooking()))
        .build();
  }

  public static TicketDTO ofTicketWithQrCode(Ticket ticket, ByteArrayOutputStream qrCode) {
    return TicketDTO.builder()
        .id(ticket.getId())
        .sightEvent(ofSightEventBasic(ticket.getSightEvent()))
        .name(ticket.getName())
        .price(ticket.getPrice())
        .date(ticket.getDate())
        .status(ticket.getStatus())
        .serialNumber(ticket.getSerialNumber())
        .booking(ofBookingBasic(ticket.getBooking()))
        .qrCode(qrCode)
        .build();
  }
}
