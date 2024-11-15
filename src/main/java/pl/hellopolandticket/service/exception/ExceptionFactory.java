package pl.hellopolandticket.service.exception;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.service.exception.badrequest.EmailSendingException;
import pl.hellopolandticket.service.exception.badrequest.EmailSendingRollbackException;
import pl.hellopolandticket.service.exception.conflict.EventDoesNotTakePlaceOnChosenDateException;
import pl.hellopolandticket.service.exception.conflict.NotBookedException;
import pl.hellopolandticket.service.exception.conflict.PunchingTicketForWrongSightException;
import pl.hellopolandticket.service.exception.conflict.RequestedDateOutsideRequestedTicketDefinitionPoolException;
import pl.hellopolandticket.service.exception.conflict.TicketConflictException;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.exception.notfound.NonRollbackResourceNotFoundException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;
import pl.hellopolandticket.service.exception.notfound.TicketNotFoundException;
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException;

@ApplicationScoped
// @Interceptors(value = LoggingHandler.class)
public class ExceptionFactory {

  @Inject
  private ExceptionMessagesService exceptionMessagesService;

  public EmailSendingException emailSendingException() {
    return new EmailSendingException(
        exceptionMessagesService.getMessage(EmailSendingException.class.getSimpleName()));
  }

  public EmailSendingRollbackException emailSendingRollbackException() {
    return new EmailSendingRollbackException(
        exceptionMessagesService.getMessage(EmailSendingRollbackException.class.getSimpleName()));
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
    return new WrongTicketStatusException(
        exceptionMessagesService.getMessage(WrongTicketStatusException.class.getSimpleName()),
        ticket);
  }

  public WrongTicketStatusException ticketAlreadyPunchedException(TicketDTO ticket) {
    return new WrongTicketStatusException(
        exceptionMessagesService.getMessage("TicketAlreadyPunchedException"), ticket);
  }

  public WrongTicketStatusException ticketInvalidException(TicketDTO ticket) {
    return new WrongTicketStatusException(
        exceptionMessagesService.getMessage("TicketInvalidException"), ticket);
  }

  public ResourceNotFoundException resourceNotFoundException() {
    return new ResourceNotFoundException(
        exceptionMessagesService.getMessage(ResourceNotFoundException.class.getSimpleName()));
  }

  public NonRollbackResourceNotFoundException nonRollbackResourceNotFoundException() {
    return new NonRollbackResourceNotFoundException(
        exceptionMessagesService.getMessage(ResourceNotFoundException.class.getSimpleName()));
  }

  public TicketNotFoundException ticketNotFoundException() {
    return new TicketNotFoundException(
        exceptionMessagesService.getMessage(TicketNotFoundException.class.getSimpleName()));
  }

  public TicketConflictException ticketForAnotherDateException() {
    return new TicketConflictException(
        exceptionMessagesService.getMessage("TicketForAnotherDateException"));
  }

  public TicketConflictException ticketTakerWithoutAccessToSightException() {
    return new TicketConflictException(
        exceptionMessagesService.getMessage("TicketTakerWithoutAccessToSightException"));
  }

  public TicketConflictException ticketBeforeEntryStartDateException() {
    return new TicketConflictException(
        exceptionMessagesService.getMessage("TicketBeforeEntryStartDateException"));
  }

  public TicketConflictException ticketAfterEntryEndDateException() {
    return new TicketConflictException(
        exceptionMessagesService.getMessage("TicketAfterEntryEndDateException"));
  }

  public TicketConflictException ticketDefinitionHasNoPoolException() {
    return new TicketConflictException(
        exceptionMessagesService.getMessage("TicketDefinitionHasNoPoolException"));
  }

  public RequestedDateOutsideRequestedTicketDefinitionPoolException requestedDateOutsideRequestedTicketDefinitionPoolException() {
    return new RequestedDateOutsideRequestedTicketDefinitionPoolException(
        exceptionMessagesService.getMessage(
            RequestedDateOutsideRequestedTicketDefinitionPoolException.class.getSimpleName()));
  }

  public CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException cannotCreateTicketPoolForNotCyclicalPoolDefinitionException() {
    return new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
        exceptionMessagesService
            .getMessage(CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException.class
                .getSimpleName()));
  }

  public EventDoesNotTakePlaceOnChosenDateException eventDoesNotTakePlaceOnChosenDateException() {
    return new EventDoesNotTakePlaceOnChosenDateException(exceptionMessagesService
        .getMessage(EventDoesNotTakePlaceOnChosenDateException.class.getSimpleName()));
  }

}
