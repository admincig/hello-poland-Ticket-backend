package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TicketTakerWithoutAccessToSightException extends ConflictBaseException {

  private static final long serialVersionUID = -2003338406106680664L;

  public TicketTakerWithoutAccessToSightException(String message) {
    this.message = message;
  }
}
