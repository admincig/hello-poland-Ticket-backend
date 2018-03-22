package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class NoAvailableTicketsException extends RuntimeException {

  private static final long serialVersionUID = 2578092518648982070L;
}
