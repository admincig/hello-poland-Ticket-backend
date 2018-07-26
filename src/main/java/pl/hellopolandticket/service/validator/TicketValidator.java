package pl.hellopolandticket.service.validator;

import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.INVALID;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicket;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
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

  public void validateTicketTakerHasAccessToSightEvent(SightEvent ticketSightEvent,
      List<SightEvent> ticketTakerSightEvents) {
    if (!ticketTakerSightEvents.contains(ticketSightEvent)) {
      throw exceptionFactory.ticketTakerWithoutAccessToSightException();
    }
  }

  public void validateProperTime(Ticket ticket) {
    Date now = new Date();
    TicketPool ticketPool = Optional.ofNullable(ticket.getTicketDefinition().getTicketPool())
        .orElseThrow(() -> exceptionFactory.ticketDefinitionHasNoPoolException());

    Date eStartDate = ticketPool.getEntryStartDate();
    Date eEndDate = ticketPool.getEntryEndDate();

    if (eStartDate != null && now.before(eStartDate)) {
      throw exceptionFactory.ticketBeforeEntryStartDateException();
    }
    if (eEndDate != null && now.after(eEndDate)) {
      throw exceptionFactory.ticketAfterEntryEndDateException();
    }
  }

}
