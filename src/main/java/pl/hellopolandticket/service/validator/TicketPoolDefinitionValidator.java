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
    if (requestedDate == null || (startDate != null && startDate.after(requestedDate))
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
    if (startDate == null || requestedDate == null) {
      throw exceptionFactory.requestedDateOutsideRequestedTicketDefinitionPoolException();
    }

    Calendar startDateCalendar = Calendar.getInstance();
    startDateCalendar.setTime(startDate);

    Calendar requestedDateCalendar = Calendar.getInstance();
    requestedDateCalendar.setTime(requestedDate);

    if (!startDateCalendar.equals(requestedDateCalendar)) {
      if (frequencyData.getFrequencyType() == DAILY) {
        long daysBetween =
            ChronoUnit.DAYS.between(startDate.toInstant(), requestedDate.toInstant());

        if (!isAnotherOccurrence(daysBetween, frequencyData.getFrequency())) {
          throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
        }
      } else if (frequencyData.getFrequencyType() == WEEKLY) {
        if (frequencyData.getDayOfWeek().getValue()
            + 1 != (requestedDateCalendar.get(Calendar.DAY_OF_WEEK))) {
          throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
        }
        long weeksBetween =
            ChronoUnit.DAYS.between(startDate.toInstant(), requestedDate.toInstant()) / 7;

        if (!isAnotherOccurrence(weeksBetween, frequencyData.getFrequency())) {
          throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
        }
      } else if (frequencyData.getFrequencyType() == MONTHLY) {
        int yearsBetween =
            requestedDateCalendar.get(Calendar.YEAR) - startDateCalendar.get(Calendar.YEAR);
        int monthsBetween = yearsBetween * 12 + requestedDateCalendar.get(Calendar.MONTH)
            - startDateCalendar.get(Calendar.MONTH);

        if (!frequencyData.getDayOfMonth().equals(requestedDateCalendar.get(Calendar.DAY_OF_MONTH))
            || !isAnotherOccurrence(monthsBetween, frequencyData.getFrequency())) {
          throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
        }
      } else if (frequencyData.getFrequencyType() == YEARLY) {
        long yearsBetween =
            requestedDateCalendar.get(Calendar.YEAR) - startDateCalendar.get(Calendar.YEAR);

        if (!frequencyData.getDayOfMonth().equals(requestedDateCalendar.get(Calendar.DAY_OF_MONTH))
            || !frequencyData.getMonth().equals(requestedDateCalendar.get(Calendar.MONTH))
            || !isAnotherOccurrence(yearsBetween, frequencyData.getFrequency())) {
          throw exceptionFactory.eventDoesNotTakePlaceOnChosenDateException();
        }
      }
    }
  }

  private boolean isAnotherOccurrence(long unitsBetween, int frequency) {
    return unitsBetween % frequency == 0;
  }
}
