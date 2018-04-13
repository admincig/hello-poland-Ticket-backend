package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PunchingTicketForWrongSightException extends RuntimeException {

  private static final long serialVersionUID = -4933601975186050651L;
}
