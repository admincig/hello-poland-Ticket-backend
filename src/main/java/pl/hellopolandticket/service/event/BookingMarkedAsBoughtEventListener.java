package pl.hellopolandticket.service.event;

import java.time.temporal.ChronoUnit;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.service.EmailSenderService;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Slf4j
@Stateless
// @Interceptors(value = LoggingHandler.class)
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailSenderService emailService;

  @Inject
  private ExceptionFactory exceptionFactory;

  @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
  @Retry(maxRetries = 5, delay = 10, delayUnit = ChronoUnit.MINUTES, jitter = 1,
      jitterDelayUnit = ChronoUnit.MINUTES, maxDuration = 60, durationUnit = ChronoUnit.MINUTES)
  @Fallback(fallbackMethod = "fallbackLogError")
  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) throws Exception {
    try {
      emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent);
    } catch (Exception e) {
      log.error(e.getMessage());
      throw e;
    }
  }

  private void fallbackLogError(BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) {
    log.error("An error occurred while sending email to: " + "["
        + bookingMarkedAsBoughtEvent.getRecipientEmail() + "]");
    throw exceptionFactory.emailSendingException();
  }

}
