package pl.hellopolandticket.service.event;

import java.time.temporal.ChronoUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.service.EmailService;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Slf4j
@ApplicationScoped
// @Interceptors(value = LoggingHandler.class)
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailService emailService;

  @Inject
  private ExceptionFactory exceptionFactory;

  @Retry(maxRetries = 5, delay = 5, delayUnit = ChronoUnit.MINUTES, jitter = 30000)
  @Fallback(fallbackMethod = "fallbackLogError")
  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) throws Exception {
    // try {
    emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent);
    // } catch (MessagingException | IOException | TemplateException e) {
    // log.error(e.getMessage());
    // throw exceptionFactory.emailSendingException();
    // }
  }

  private void fallbackLogError(BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) {
    log.error(
        "An error occurred while sending email to: " + bookingMarkedAsBoughtEvent.getCustomerName()
            + "[" + bookingMarkedAsBoughtEvent.getRecipientEmail() + "]");
    throw exceptionFactory.emailSendingException();
  }

}
