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
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
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
      EmailSendingException ex = new EmailSendingException(
        exceptionMessagesService.getMessage(EmailSendingException.class.getSimpleName()));

      ex.setErrorKey(EmailSendingException.class.getSimpleName());
      return ex;

  }

  public EmailSendingRollbackException emailSendingRollbackException() {
      EmailSendingRollbackException ex = new EmailSendingRollbackException(
        exceptionMessagesService.getMessage(EmailSendingRollbackException.class.getSimpleName()));
      ex.setErrorKey(EmailSendingRollbackException.class.getSimpleName());
      return ex;
  }

  public NotBookedException notBookedException() {
      NotBookedException ex = new NotBookedException(
        exceptionMessagesService.getMessage(NotBookedException.class.getSimpleName()));

      ex.setErrorKey(NotBookedException.class.getSimpleName());
      return ex;
  }

  public PunchingTicketForWrongSightException punchingTicketForWrongSightException() {
      PunchingTicketForWrongSightException ex = new PunchingTicketForWrongSightException(exceptionMessagesService
        .getMessage(PunchingTicketForWrongSightException.class.getSimpleName()));
      ex.setErrorKey(PunchingTicketForWrongSightException.class.getSimpleName());
      return ex;
  }

  public WrongTicketStatusException wrongTicketStatusException(TicketDTO ticket) {
      WrongTicketStatusException ex = new WrongTicketStatusException(
        exceptionMessagesService.getMessage(WrongTicketStatusException.class.getSimpleName()),
        ticket);
      ex.setErrorKey(WrongTicketStatusException.class.getSimpleName());
      return ex;
  }

    public WrongTicketStatusException ticketAlreadyPunchedException(TicketDTO ticket) {
        WrongTicketStatusException ex =
                new WrongTicketStatusException(
                        exceptionMessagesService.getMessage("TicketAlreadyPunchedException"),
                        ticket
                );
        ex.setErrorKey("TicketAlreadyPunchedException");
        return ex;
    }

  public WrongTicketStatusException ticketInvalidException(TicketDTO ticket) {
    WrongTicketStatusException ex = new WrongTicketStatusException(
        exceptionMessagesService.getMessage("TicketInvalidException"), ticket);
    ex.setErrorKey("TicketInvalidException");
    return ex;
  }

  public ResourceNotFoundException resourceNotFoundException() {
      ResourceNotFoundException ex =  new ResourceNotFoundException(
        exceptionMessagesService.getMessage(ResourceNotFoundException.class.getSimpleName()));
      ex.setErrorKey(ResourceNotFoundException.class.getSimpleName());
      return ex;
  }

  public NonRollbackResourceNotFoundException nonRollbackResourceNotFoundException() {
      NonRollbackResourceNotFoundException ex = new NonRollbackResourceNotFoundException(
        exceptionMessagesService.getMessage(ResourceNotFoundException.class.getSimpleName()));
      ex.setErrorKey(ResourceNotFoundException.class.getSimpleName());
      return ex;
  }

  public TicketNotFoundException ticketNotFoundException() {
      TicketNotFoundException ex = new TicketNotFoundException(
        exceptionMessagesService.getMessage(TicketNotFoundException.class.getSimpleName()));
      ex.setErrorKey(TicketNotFoundException.class.getSimpleName());
      return ex;
  }

  public TicketConflictException ticketForAnotherDateException() {
      TicketConflictException ex =  new TicketConflictException(
        exceptionMessagesService.getMessage("TicketForAnotherDateException"));
      ex.setErrorKey("TicketForAnotherDateException");
      return ex;
  }

  public TicketConflictException ticketTakerWithoutAccessToSightException() {
      TicketConflictException ex= new TicketConflictException(
        exceptionMessagesService.getMessage("TicketTakerWithoutAccessToSightException"));
      ex.setErrorKey("TicketTakerWithoutAccessToSightException");
      return ex;

  }

  public TicketConflictException ticketBeforeEntryStartDateException() {
      TicketConflictException ex = new TicketConflictException(
        exceptionMessagesService.getMessage("TicketBeforeEntryStartDateException"));
      ex.setErrorKey("TicketBeforeEntryStartDateException");
      return ex;
  }

  public TicketConflictException ticketAfterEntryEndDateException() {
      TicketConflictException ex = new TicketConflictException(
        exceptionMessagesService.getMessage("TicketAfterEntryEndDateException"));
      ex.setErrorKey("TicketAfterEntryEndDateException");
      return ex;
  }

  public TicketConflictException ticketDefinitionHasNoPoolException() {
      TicketConflictException ex =  new TicketConflictException(
        exceptionMessagesService.getMessage("TicketDefinitionHasNoPoolException"));
      ex.setErrorKey("TicketDefinitionHasNoPoolException");
      return ex;
  }

  public RequestedDateOutsideRequestedTicketDefinitionPoolException requestedDateOutsideRequestedTicketDefinitionPoolException() {
      RequestedDateOutsideRequestedTicketDefinitionPoolException ex =  new RequestedDateOutsideRequestedTicketDefinitionPoolException(
        exceptionMessagesService.getMessage(
            RequestedDateOutsideRequestedTicketDefinitionPoolException.class.getSimpleName()));
      ex.setErrorKey(RequestedDateOutsideRequestedTicketDefinitionPoolException.class.getSimpleName());
      return ex;
  }

  public CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException cannotCreateTicketPoolForNotCyclicalPoolDefinitionException() {
      CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException ex = new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
        exceptionMessagesService
            .getMessage(CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException.class
                .getSimpleName()));
      ex.setErrorKey(CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException.class.getSimpleName());
      return ex;
  }

  public EventDoesNotTakePlaceOnChosenDateException eventDoesNotTakePlaceOnChosenDateException() {
      EventDoesNotTakePlaceOnChosenDateException ex = new EventDoesNotTakePlaceOnChosenDateException(exceptionMessagesService
        .getMessage(EventDoesNotTakePlaceOnChosenDateException.class.getSimpleName()));

      ex.setErrorKey(EventDoesNotTakePlaceOnChosenDateException.class.getSimpleName());
    return  ex;
  }

  public ConflictingException partnerAlreadyExistsException() {
    ConflictingException ex = new ConflictingException(
        exceptionMessagesService.getMessage("PartnerAlreadyExistsException"));
    ex.setErrorKey("PartnerAlreadyExistsException");
    return ex;
  }

}
