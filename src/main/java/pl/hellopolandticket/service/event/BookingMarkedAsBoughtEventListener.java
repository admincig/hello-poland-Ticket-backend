package pl.hellopolandticket.service.event;

import freemarker.template.TemplateException;
import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.mail.MessagingException;
import pl.hellopolandticket.service.EmailService;

@ApplicationScoped
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailService emailService;

  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent)
      throws MessagingException, IOException, TemplateException {
    emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent.getCustomerName(),
        bookingMarkedAsBoughtEvent.getCustomerEmail(),
        bookingMarkedAsBoughtEvent.getTickets(),
        bookingMarkedAsBoughtEvent.getTicketQrCodes());
  }
}
