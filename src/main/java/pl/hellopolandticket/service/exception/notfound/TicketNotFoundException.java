package pl.hellopolandticket.service.exception.notfound;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class TicketNotFoundException extends ResourceNotFoundException {
  private static final long serialVersionUID = 2254638518945420488L;

  public TicketNotFoundException(String message) {
    super(message);
  }

}
