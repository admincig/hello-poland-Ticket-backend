package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class RequestedDateOutsideRequestedTicketDefinitionPoolException
    extends ConflictBaseException {

  private static final long serialVersionUID = 7672867008427805861L;

  public RequestedDateOutsideRequestedTicketDefinitionPoolException(String message) {
    this.message = message;
  }

}
