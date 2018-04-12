package pl.hellopolandticket.service.event;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.service.dto.TicketDTO;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingMarkedAsBoughtEvent implements Serializable {

  private static final long serialVersionUID = 6300346760308664459L;

  private String customerName;
  private String customerEmail;
  private List<TicketDTO> tickets;
  private List<ByteArrayOutputStream> ticketQrCodes;
}
