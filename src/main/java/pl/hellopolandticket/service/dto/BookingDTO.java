package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.model.Status;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO implements Serializable {

  private static final long serialVersionUID = 2556193666908234466L;

  private Long id;
  private String customerName;
  private String customerEmail;
  private Status status;
  private String serialNumber;
  private List<TicketDTO> tickets;

  public static BookingDTO ofBooking(Booking booking) {
    return BookingDTO.builder()
        .id(booking.getId())
        .customerName(booking.getCustomerName())
        .customerEmail(booking.getCustomerEmail())
        .status(booking.getStatus())
        .serialNumber(booking.getSerialNumber())
        .tickets(booking.getTickets().stream()
            .map(TicketDTO::ofTicket)
            .collect(Collectors.toList()))
        .build();
  }

  public static BookingDTO ofBookingBasic(Booking booking) {
    return BookingDTO.builder()
        .id(booking.getId())
        .customerName(booking.getCustomerName())
        .customerEmail(booking.getCustomerEmail())
        .build();
  }
}