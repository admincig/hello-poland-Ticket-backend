package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TicketForAnotherDateException extends ConflictBaseException {

  private static final long serialVersionUID = -8191749578287235520L;

  public TicketForAnotherDateException(String message) {
    this.message = message;
  }
}
