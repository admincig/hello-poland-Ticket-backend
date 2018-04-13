package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NoAvailableTicketsException extends ConflictBaseException {

  private static final long serialVersionUID = -5286998634370398280L;
}
