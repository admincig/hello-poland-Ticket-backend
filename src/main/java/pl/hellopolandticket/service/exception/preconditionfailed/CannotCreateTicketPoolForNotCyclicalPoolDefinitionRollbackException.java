package pl.hellopolandticket.service.exception.preconditionfailed;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CannotCreateTicketPoolForNotCyclicalPoolDefinitionRollbackException
    extends PreconditionFailedBaseException {

  private static final long serialVersionUID = 529628999976462592L;

  public CannotCreateTicketPoolForNotCyclicalPoolDefinitionRollbackException(String message) {
    this.message = message;
  }
}
