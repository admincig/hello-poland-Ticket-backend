package pl.hellopolandticket.service.dto;

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
public class TicketDefinitionNumberDTO {

  @NotNull
  private Long ticketDefinitionId;

  @NotNull
  private Long numberOfTickets;
}
