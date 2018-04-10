package pl.hellopolandticket.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.hellopolandticket.service.dto.TicketDTO;

@Getter
@AllArgsConstructor
public class WrongTicketStatusException extends RuntimeException {

  private static final long serialVersionUID = -5731726281240039208L;

  public TicketDTO ticket;

}
