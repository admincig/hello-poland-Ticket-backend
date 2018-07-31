package pl.hellopolandticket.service.exception.conflict;


import pl.hellopoland.dto.booking.TicketDTO;

public class TicketInvalidException extends WrongTicketStatusException {
  private static final long serialVersionUID = 4820443580601739066L;

  public TicketInvalidException(String message, TicketDTO ticket) {
    super(message, ticket);
  }

}
