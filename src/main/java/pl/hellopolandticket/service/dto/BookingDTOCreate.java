package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTOCreate implements Serializable {

  private static final long serialVersionUID = -3098157182713594498L;

  @NotNull
  private String customerName;

  @NotNull
  private String customerEmail;

  @NotNull
  private List<TicketBookingDTO> ticketBookings;
}