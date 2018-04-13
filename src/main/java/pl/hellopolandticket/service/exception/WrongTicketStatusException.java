package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.hellopolandticket.service.dto.TicketDTO;

@Getter
@AllArgsConstructor
@ApplicationException(rollback = true)
public class WrongTicketStatusException extends RuntimeException {

  private static final long serialVersionUID = -5731726281240039208L;

  public TicketDTO ticket;

}
