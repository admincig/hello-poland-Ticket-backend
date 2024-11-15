package pl.hellopolandticket.service.exception.conflict;

import jakarta.ejb.ApplicationException;
import lombok.Builder;
import lombok.Getter;
import pl.hellopoland.dto.booking.TicketDTO;

@Getter
@ApplicationException(rollback = true)
public class WrongTicketStatusException extends ConflictBaseException {

  private static final long serialVersionUID = 8909641422743727192L;

  public TicketDTO ticket;

  @Builder
  public WrongTicketStatusException(String message, TicketDTO ticket) {
    this.message = message;
    this.ticket = ticket;
  }

}
