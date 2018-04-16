package pl.hellopolandticket.service.exception.conflict;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NotBookedException extends ConflictBaseException {

  private static final long serialVersionUID = -5329810822328213548L;
}
