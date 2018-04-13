package pl.hellopolandticket.service.event;

import freemarker.template.TemplateException;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.service.EmailService;
import pl.hellopolandticket.service.exception.EmailSendingException;

@Slf4j
@ApplicationScoped
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailService emailService;

  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) {
    try {
      emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent.getCustomerName(),
          bookingMarkedAsBoughtEvent.getCustomerEmail(),
          bookingMarkedAsBoughtEvent.getTickets());
    } catch (MessagingException | IOException | TemplateException e) {
      log.error(e.getMessage());
      throw new EmailSendingException();
    }
  }
}
