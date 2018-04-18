package pl.hellopolandticket.service.validator;

import static pl.hellopolandticket.model.DateType.DATE;
import static pl.hellopolandticket.model.DateType.DATE_TIME;
import static pl.hellopolandticket.model.Status.BOUGHT;
import static pl.hellopolandticket.model.Status.INVALID;
import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@RequestScoped
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

  public void validateProperTime(Ticket ticket) {
    if (ticket.getDateType() == DATE) {
      Date ticketDate = ticket.getDate();
      Date sightDate = ticket.getSight().getDate();

      if (!(ticketDate.getDay() == sightDate.getDay() && ticketDate.getMonth() == sightDate
          .getMonth() && ticketDate.getYear() == sightDate.getYear())) {
        throw exceptionFactory.ticketForAnotherDateException();
      }
    } else if (ticket.getDateType() == DATE_TIME) {
      if (ticket.getDate().getTime() != ticket.getSight().getDate().getTime()) {
        throw exceptionFactory.ticketForAnotherDateException();
      }
    }
  }
}
