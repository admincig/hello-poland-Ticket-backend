package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TicketConflictException extends ConflictBaseException {
  private static final long serialVersionUID = 1L;

  public TicketConflictException(String message) {
    this.message = message;
  }

}
