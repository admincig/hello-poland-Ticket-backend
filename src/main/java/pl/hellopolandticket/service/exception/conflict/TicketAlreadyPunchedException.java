package pl.hellopolandticket.service.exception.conflict;


import pl.hellopoland.dto.booking.TicketDTO;

public class TicketAlreadyPunchedException extends WrongTicketStatusException {
  private static final long serialVersionUID = 1633446645757660782L;

  public TicketAlreadyPunchedException(String message, TicketDTO ticket) {
    super(message, ticket);
  }
}
