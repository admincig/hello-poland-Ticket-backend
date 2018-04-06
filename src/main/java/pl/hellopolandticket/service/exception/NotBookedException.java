package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NotBookedException extends RuntimeException {

  private static final long serialVersionUID = 1021967320578180528L;
}
