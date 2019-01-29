package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConflictingException extends ConflictBaseException {
  private static final long serialVersionUID = -2347288019537878183L;

  public ConflictingException(String message) {
    this.message = message;
  }

  // public ConflictingException(String message, Throwable cause) {
  // super(message, cause);
  // }
}
