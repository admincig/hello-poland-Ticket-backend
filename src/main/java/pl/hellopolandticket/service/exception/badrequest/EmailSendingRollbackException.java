package pl.hellopolandticket.service.exception.badrequest;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EmailSendingRollbackException extends EmailSendingException {
  private static final long serialVersionUID = 8793798416343852834L;

  public EmailSendingRollbackException(String message) {
    super(message);
  }
}
