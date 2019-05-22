package pl.hellopolandticket.service.validator;

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@RequestScoped
// @Interceptors(value = LoggingHandler.class)
public class TicketPoolValidator {

  @Inject
  private ExceptionFactory exceptionFactory;

  public void validateRequestedDateEqualsStartDate(Date startDate, Date requestedDate) {
    if (startDate == null || !startDate.equals(requestedDate)) {
      throw exceptionFactory.requestedDateOutsideRequestedTicketDefinitionPoolException();
    }
  }

}
