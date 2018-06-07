package pl.hellopolandticket.service.event;

import freemarker.template.TemplateException;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.service.EmailService;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Slf4j
@ApplicationScoped
@Interceptors(value = LoggingHandler.class)
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailService emailService;

  @Inject
  private ExceptionFactory exceptionFactory;

  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) {
    try {
      emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent.getCustomerName(),
          bookingMarkedAsBoughtEvent.getCustomerEmail(),
          bookingMarkedAsBoughtEvent.getTickets());
    } catch (MessagingException | IOException | TemplateException e) {
      log.error(e.getMessage());
      throw exceptionFactory.emailSendingException();
    }
  }
}
