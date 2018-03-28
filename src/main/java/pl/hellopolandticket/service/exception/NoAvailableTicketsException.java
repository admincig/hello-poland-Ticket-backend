package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NoAvailableTicketsException extends RuntimeException {

  private static final long serialVersionUID = 2578092518648982070L;
}
