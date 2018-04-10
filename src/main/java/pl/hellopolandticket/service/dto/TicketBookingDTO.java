package pl.hellopolandticket.service.dto;

import java.io.Serializable;
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
public class TicketBookingDTO implements Serializable {

  private static final long serialVersionUID = -5790760386014306539L;

  @NotNull
  private Long ticketDefinitionId;

  @NotNull
  private Long numberOfTickets;
}