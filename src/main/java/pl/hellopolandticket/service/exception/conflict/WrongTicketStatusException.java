package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pl.hellopolandticket.service.dto.TicketDTO;

@Getter
@AllArgsConstructor
@ApplicationException(rollback = true)
public class WrongTicketStatusException extends ConflictBaseException {

  private static final long serialVersionUID = 8909641422743727192L;

  public TicketDTO ticket;

}
