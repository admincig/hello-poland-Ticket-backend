package pl.hellopolandticket.service.event;

import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.service.EmailSenderService;
import pl.hellopolandticket.service.exception.ExceptionFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

@Slf4j
@Stateless
public class BookingMarkedAsBoughtEventListener {

  @Inject
  private EmailSenderService emailService;

  @Inject
  private ExceptionFactory exceptionFactory;

  @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
  public void bookingMarkedAsBoughtEventHandler(
      @ObservesAsync BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent) throws Exception {
    try {
      emailService.sendEmailWithQrCodes(bookingMarkedAsBoughtEvent);
    } catch (Exception e) {
      log.error("An error occurred while sending email to: " + "["
          + bookingMarkedAsBoughtEvent.getRecipientEmail() + "]", e);
      exceptionFactory.emailSendingException();
    }
  }

}
