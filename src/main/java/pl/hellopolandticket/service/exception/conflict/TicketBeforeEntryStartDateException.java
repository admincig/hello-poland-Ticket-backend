package pl.hellopolandticket.service.exception.conflict;

public class TicketBeforeEntryStartDateException extends ConflictBaseException {
  private static final long serialVersionUID = -3684244282866392819L;

  public TicketBeforeEntryStartDateException(String message) {
    this.message = message;
  }

}
