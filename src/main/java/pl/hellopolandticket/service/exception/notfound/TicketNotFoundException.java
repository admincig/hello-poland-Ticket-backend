package pl.hellopolandticket.service.exception.notfound;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TicketNotFoundException extends ResourceNotFoundException {

  public TicketNotFoundException(String message) {
    super(message);
  }

}
