package pl.hellopolandticket.service.validator;

import static pl.hellopolandticket.model.ticket.partner.FrequencyType.DAILY;
import static pl.hellopolandticket.model.ticket.partner.FrequencyType.MONTHLY;
import static pl.hellopolandticket.model.ticket.partner.FrequencyType.WEEKLY;
import static pl.hellopolandticket.model.ticket.partner.FrequencyType.YEARLY;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@RequestScoped
@Interceptors(value = LoggingHandler.class)
public class TicketPoolDefinitionValidator {

  @Inject
  private ExceptionFactory exceptionFactory;

  public void validateRequestedDateBetweenStartDateAndEndDate(Date startDate, Date endDate,
      Date requestedDate) {
    if ((startDate != null && startDate.after(requestedDate))
        || (endDate != null && endDate.before(requestedDate))) {
      throw exceptionFactory.requestedDateOutsideRequestedTicketDefinitionPoolException();
    }
  }

  public void validateCyclicalPool(Boolean cyclicalPool, FrequencyData frequencyData) {
    if (!cyclicalPool || frequencyData == null) {
      throw exceptionFactory.cannotCreateTicketPoolForNotCyclicalPoolDefinitionException();
    }
  }

  public void validateFrequencyDataPermitsToCreateTicketPool(FrequencyData frequencyData,
      Date startDate, Date requestedDate) {
    Calendar startDateCalendar = Calendar.getInstance();
    startDateCalendar.setTime(requestedDate);

    Calendar requestedDateCalendar = Calendar.getInstance();
    requestedDateCalendar.setTime(requestedDate);

    if (frequencyData.getFrequencyType() == DAILY) {
      long daysBetween = ChronoUnit.DAYS.between(startDate.toInstant(), requestedDate.toInstant());

      if (!isAnotherOccurrence(daysBetween, frequencyData.getFrequency())) {
        throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
      }
    } else if (frequencyData.getFrequencyType() == WEEKLY) {
      long weeksBetween = ChronoUnit.WEEKS
          .between(startDate.toInstant(), requestedDate.toInstant());

      if (startDateCalendar.get(Calendar.DAY_OF_WEEK) != requestedDateCalendar
          .get(Calendar.DAY_OF_WEEK)
          || !isAnotherOccurrence(weeksBetween, frequencyData.getFrequency())) {
        throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
      }
    } else if (frequencyData.getFrequencyType() == MONTHLY) {
      long monthsBetween = ChronoUnit.MONTHS
          .between(startDate.toInstant(), requestedDate.toInstant());

      if (startDateCalendar.get(Calendar.DAY_OF_MONTH) != requestedDateCalendar
          .get(Calendar.DAY_OF_MONTH)
          || isAnotherOccurrence(monthsBetween, frequencyData.getFrequency())) {
        throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
      }
    } else if (frequencyData.getFrequencyType() == YEARLY) {
      long yearsBetween = ChronoUnit.YEARS
          .between(startDate.toInstant(), requestedDate.toInstant());

      if (startDateCalendar.get(Calendar.DAY_OF_MONTH) != requestedDateCalendar
          .get(Calendar.DAY_OF_MONTH)
          || startDateCalendar.get(Calendar.MONTH) != requestedDateCalendar.get(Calendar.MONTH)
          || isAnotherOccurrence(yearsBetween, frequencyData.getFrequency())) {
        throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
      }
    }
  }

  private boolean isAnotherOccurrence(long unitsBetween, int frequency) {
    return unitsBetween % frequency == 0;
  }
}
