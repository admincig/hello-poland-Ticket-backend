package pl.hellopolandticket.service.validator;

import static pl.hellopolandticket.model.Status.BOUGHT;
import static pl.hellopolandticket.model.Status.INVALID;
import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@RequestScoped
@Interceptors(value = LoggingHandler.class)
public class TicketValidator {

  @Inject
  private ExceptionFactory exceptionFactory;

  public void validateAccessingProperTicket(Long firstSightId, Long secondSightId) {
    if (!firstSightId.equals(secondSightId)) {
      throw exceptionFactory.punchingTicketForWrongSightException();
    }
  }

  public void validateTicketHasDemandedStatus(Ticket ticket) {
    if (ticket.getStatus() == PUNCHED) {
      throw exceptionFactory.ticketAlreadyPunchedException(ofTicket(ticket));
    } else if (ticket.getStatus() == INVALID) {
      throw exceptionFactory.ticketInvalidException(ofTicket(ticket));
    } else if (ticket.getStatus() != BOUGHT) {
      throw exceptionFactory.wrongTicketStatusException(ofTicket(ticket));
    }
  }

  public void validateTicketTakerHasAccessToSight(SightEvent ticketSightEvent,
      List<SightEvent> ticketTakerSightEvents) {
    if (!ticketTakerSightEvents.contains(ticketSightEvent)) {
      throw exceptionFactory.ticketTakerWithoutAccessToSightException();
    }
  }

  public void validateProperTime(Ticket ticket) {
    //TODO Implement when you divide sights and events
  }
}
