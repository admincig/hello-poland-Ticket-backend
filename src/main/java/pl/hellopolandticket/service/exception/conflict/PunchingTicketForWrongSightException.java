package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;
import pl.hellopolandticket.service.exception.preconditionfailed.PreconditionFailedBaseException;

@ApplicationException(rollback = true)
public class PunchingTicketForWrongSightException extends PreconditionFailedBaseException {

  private static final long serialVersionUID = 9007571961681767465L;

  public PunchingTicketForWrongSightException(String message) {
    this.message = message;
  }
}
