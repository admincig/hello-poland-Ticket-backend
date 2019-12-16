package pl.hellopolandticket.service;

import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketPoolQuantityMonitoringService extends ServiceSuperclass {

  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private EmailSenderService emailService;

  public void informPartnerAboutTicketsNumberLeftToBuyRunningOut(TicketPool ticketPool) {
    if (shouldInform(ticketPool)) {
      constructAndSendEmail(ticketPool);
    }
  }

  private boolean shouldInform(TicketPool ticketPool) {
    int left = ticketPool.getTicketsLeftToBuyCount();
    return left == 0 || left == 3;
  }

  private void constructAndSendEmail(TicketPool ticketPool) {
    TicketsRunningOutEmailConstructor emailConstructor =
        new TicketsRunningOutEmailConstructor(ticketPool);
    recipientsToInformAboutTicketsRunningOut(ticketPool).forEach(recipient -> {
      try {
        emailService.sendSimpleEmail(recipient, emailConstructor.getTitle(),
            emailConstructor.getContent());
        logger.log(Logger.Level.INFO,
            "Informing " + recipient + " about tickets from ticket pool[id="
                + ticketPool.getId() + "] running out.");
      } catch (MessagingException | UnsupportedEncodingException e) {
        throw exceptionFactory.emailSendingRollbackException();
      }
    });
  }

  private Set<String> recipientsToInformAboutTicketsRunningOut(TicketPool ticketPool) {
    Set<String> all = new HashSet<>();
    all.add(properties.getProperty("mail.hellopoland.biuro"));
    all.add(ticketPool.getTicketPoolDefinition().getSightEvent().getPartner().getEmail());
    return all;
  }



  // private boolean shouldInform123(Ticket ticket) {
  // Long numberOfAllTicketsYouCanBuyFromPool =
  // Long.valueOf(ticket.getTicketPool().getTicketPoolDefinition().getAvailableTicketsNumber());
  //
  // Long ticketsNumberLeftToBuyFromPool =numberOfTicketsLeftToBuyFromPool(ticket);
  //
  // if((numberOfAllTicketsYouCanBuyFromPool >0 && shouldInform(ticketsNumberLeftToBuyFromPool))
  // ||
  // (numberOfAllTicketsYouCanBuyFromPool.equals(-1L))
  // ) {
  // informPartnerAboutTicketsNumberLeftToBuyRunningOut(ticket, ticketsNumberLeftToBuyFromPool);
  // }
  //
  // }

}
