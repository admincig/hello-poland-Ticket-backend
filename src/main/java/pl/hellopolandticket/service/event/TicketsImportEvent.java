package pl.hellopolandticket.service.event;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopoland.dto.Ticket;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketsImportEvent implements Serializable {

  private static final long serialVersionUID = -6531664193832568648L;

  private List<Ticket> tickets;

}