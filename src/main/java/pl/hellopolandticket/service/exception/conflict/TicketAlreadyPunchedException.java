package pl.hellopolandticket.service.exception.conflict;

import pl.hellopolandticket.service.dto.TicketDTO;

public class TicketAlreadyPunchedException extends WrongTicketStatusException {

  public TicketAlreadyPunchedException(String message, TicketDTO ticket) {
    super(message, ticket);
  }
}
