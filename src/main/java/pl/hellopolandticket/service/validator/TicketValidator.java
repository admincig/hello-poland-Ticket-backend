package pl.hellopolandticket.service.validator;

import static java.util.stream.Collectors.toList;

import java.util.List;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

public class TicketValidator {

  public static void validateFoundAllTheTickets(List<Ticket> tickets, List<Long> ticketIds) {
    if (!tickets.stream().map(Ticket::getId).collect(toList()).equals(ticketIds)) {
      throw new ResourceNotFoundException();
    }
  }
}
