package pl.hellopolandticket.service.exception.preconditionfailed;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CannotCreateTicketPoolForNotCyclicalPoolDefinitionException
    extends PreconditionFailedBaseException {

  private static final long serialVersionUID = 529628999976462592L;

  public CannotCreateTicketPoolForNotCyclicalPoolDefinitionException(String message) {
    this.message = message;
  }
}
