package pl.hellopolandticket.service.exception.conflict;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NoAvailableTicketsException extends ConflictBaseException {

  public NoAvailableTicketsException() {
  }
  public NoAvailableTicketsException(String message) {
    this.message = message;
  }

  private static final long serialVersionUID = -5286998634370398280L;

}
