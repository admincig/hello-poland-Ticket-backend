package pl.hellopolandticket.service.exception.conflict;

import pl.hellopolandticket.service.dto.TicketDTO;

public class TicketInvalidException extends WrongTicketStatusException {

  public TicketInvalidException(String message, TicketDTO ticket) {
    super(message, ticket);
  }

}
