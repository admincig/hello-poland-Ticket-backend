package pl.hellopolandticket.service.exception;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.exception.badrequest.EmailSendingException;
import pl.hellopolandticket.service.exception.badrequest.ImportingDataException;
import pl.hellopolandticket.service.exception.conflict.NotBookedException;
import pl.hellopolandticket.service.exception.conflict.PunchingTicketForWrongSightException;
import pl.hellopolandticket.service.exception.conflict.TicketAlreadyPunchedException;
import pl.hellopolandticket.service.exception.conflict.TicketForAnotherDateException;
import pl.hellopolandticket.service.exception.conflict.TicketInvalidException;
import pl.hellopolandticket.service.exception.conflict.TicketTakerWithoutAccessToSightException;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;
import pl.hellopolandticket.service.exception.notfound.TicketNotFoundException;

@ApplicationScoped
public class ExceptionFactory {

  @Inject
  private ExceptionMessagesService exceptionMessagesService;

  public EmailSendingException emailSendingException() {
    return new EmailSendingException(
        exceptionMessagesService.getMessage(EmailSendingException.class.getSimpleName()));
  }

  public ImportingDataException importingDataException() {
    return new ImportingDataException(
        exceptionMessagesService.getMessage(ImportingDataException.class.getSimpleName()));
  }

  public NotBookedException notBookedException() {
    return new NotBookedException(
        exceptionMessagesService.getMessage(NotBookedException.class.getSimpleName()));
  }

  public PunchingTicketForWrongSightException punchingTicketForWrongSightException() {
    return new PunchingTicketForWrongSightException(exceptionMessagesService
        .getMessage(PunchingTicketForWrongSightException.class.getSimpleName()));
  }

  public WrongTicketStatusException wrongTicketStatusException(TicketDTO ticket) {
    return new WrongTicketStatusException(exceptionMessagesService
        .getMessage(WrongTicketStatusException.class.getSimpleName()), ticket);
  }

  public ResourceNotFoundException resourceNotFoundException() {
    return new ResourceNotFoundException(
        exceptionMessagesService.getMessage(ResourceNotFoundException.class.getSimpleName()));
  }

  public TicketNotFoundException ticketNotFoundException() {
    return new TicketNotFoundException(
        exceptionMessagesService.getMessage(TicketNotFoundException.class.getSimpleName()));
  }

  public TicketAlreadyPunchedException ticketAlreadyPunchedException(TicketDTO ticket) {
    return new TicketAlreadyPunchedException(
        exceptionMessagesService.getMessage(TicketAlreadyPunchedException.class.getSimpleName()),
        ticket);
  }

  public TicketInvalidException ticketInvalidException(TicketDTO ticket) {
    return new TicketInvalidException(
        exceptionMessagesService.getMessage(TicketInvalidException.class.getSimpleName()), ticket);
  }

  public TicketForAnotherDateException ticketForAnotherDateException() {
    return new TicketForAnotherDateException(
        exceptionMessagesService.getMessage(TicketForAnotherDateException.class.getSimpleName()));
  }

  public TicketTakerWithoutAccessToSightException ticketTakerWithoutAccessToSightException() {
    return new TicketTakerWithoutAccessToSightException(
        exceptionMessagesService
            .getMessage(TicketTakerWithoutAccessToSightException.class.getSimpleName()));
  }
}
