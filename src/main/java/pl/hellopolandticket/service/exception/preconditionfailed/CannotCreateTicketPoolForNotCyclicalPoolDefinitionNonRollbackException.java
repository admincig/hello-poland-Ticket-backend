package pl.hellopolandticket.service.exception.preconditionfailed;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException
    extends PreconditionFailedBaseException {

  private static final long serialVersionUID = 529628999976462592L;

  public CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(String message) {
    this.message = message;
  }
}
