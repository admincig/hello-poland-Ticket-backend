package pl.hellopolandticket.service.exception.badrequest;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CannotGenerateTicketSerialNumberException extends BadRequestBaseException {
  private static final long serialVersionUID = 1531110032799988507L;

  public CannotGenerateTicketSerialNumberException() {
    super();
    this.message = "Could not generate serial number for ticket";
  }

}
