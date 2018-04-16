package pl.hellopolandticket.service.validator;

import static pl.hellopolandticket.model.Status.BOUGHT;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;

import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.exception.conflict.PunchingTicketForWrongSightException;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;

public class TicketValidator {

  public static void validateAccessingProperTicket(Long firstSightId, Long secondSightId) {
    if (!firstSightId.equals(secondSightId)) {
      throw new PunchingTicketForWrongSightException();
    }
  }

  public static void validateTicketHasDemandedStatus(Ticket ticket) {
    if (ticket.getStatus() != BOUGHT) {
      throw new WrongTicketStatusException(ofTicket(ticket));
    }
  }
}
