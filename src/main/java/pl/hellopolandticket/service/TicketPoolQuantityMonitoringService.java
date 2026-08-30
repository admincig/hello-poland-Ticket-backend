package pl.hellopolandticket.service;

import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.Limited;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketPoolQuantityMonitoringService extends ServiceSuperclass {

  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private EmailSenderService emailService;

  public void informPartnerAboutTicketsNumberLeftToBuyRunningOut(List<Ticket> tickets) {
    Stream<TicketPool> poolsStream = tickets.stream()
        .map(Ticket::getTicketPool)
        .distinct();
    informPartnerAboutPoolsRunningOut(poolsStream);

    Stream<AvailableTicketNumberAssociation> atnaStream = tickets
        .stream()
        .map(t -> t.getTicketDefinition().getAtna(t.getTicketPool()))
        .distinct();
    informPartnerAboutAtnasRunningOut(atnaStream);
  }

  public void informPartnerAboutPoolsRunningOut(Stream<TicketPool> poolsStream) {
    poolsStream
        .filter(this::shouldInform)
        .forEach(this::constructAndSendEmail);
  }

  public void informPartnerAboutAtnasRunningOut(
      Stream<AvailableTicketNumberAssociation> atnaStream) {
    atnaStream
        .filter(this::shouldInform)
        .forEach(this::constructAndSendEmail);
  }

  private boolean shouldInform(Limited limited) {
    int left = limited.getTicketsLeftToBuyCount();
    //return left >= 0 && left <= 3;
      return left==0;

  }

  private void constructAndSendEmail(TicketPool ticketPool) {
    TicketsRunningOutEmailConstructor emailConstructor =
        new TicketsRunningOutEmailConstructor(ticketPool);
    recipientsToInformAboutTicketsRunningOut(ticketPool).forEach(recipient -> {
      try {
        emailService.sendSystemEmail(recipient, emailConstructor.getTitle(),
            emailConstructor.getContent());
        logger.log(Logger.Level.INFO,
            "Informing " + recipient + " about tickets from ticket pool[id="
                + ticketPool.getId() + "] running out.");
      } catch (MessagingException | UnsupportedEncodingException e) {
        throw exceptionFactory.emailSendingRollbackException();
      }
    });
  }

    private void constructAndSendEmail(AvailableTicketNumberAssociation atna) {
        TicketPool pool = atna.getTicketPool();

        // jeśli cała pula jest już wyprzedana, to mail z poola wystarczy
        if (shouldInform(pool)) {
            logger.log(Logger.Level.INFO,
                    "Skipping ATNA notification because ticket pool[id=" + pool.getId()
                            + "] is already sold out.");
            return;
        }

        TicketsRunningOutEmailConstructor emailConstructor =
                new TicketsRunningOutEmailConstructor(atna);

        recipientsToInformAboutTicketsRunningOut(pool).forEach(recipient -> {
            try {
                emailService.sendSystemEmail(recipient, emailConstructor.getTitle(),
                        emailConstructor.getContent());
                logger.log(Logger.Level.INFO,
                        "Informing " + recipient + " about tickets from ticket pool[id="
                                + pool.getId() + "] running out.");
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw exceptionFactory.emailSendingRollbackException();
            }
        });
    }

    private Set<String> recipientsToInformAboutTicketsRunningOut(TicketPool ticketPool) {
        Set<String> all = new HashSet<>();

        String helpdeskEmail = properties.getProperty("mail.hellopoland.helpdesk");
        if (helpdeskEmail != null && !helpdeskEmail.isBlank()) {
            all.add(helpdeskEmail);
        }

        String partnerEmail = ticketPool.getTicketPoolDefinition().getSightEvent().getPartner().getEmail();
        if (partnerEmail != null && !partnerEmail.isBlank()) {
            all.add(partnerEmail);
        }

        return all;
    }

}
