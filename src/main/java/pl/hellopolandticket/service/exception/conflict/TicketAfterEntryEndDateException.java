package pl.hellopolandticket.service.exception.conflict;

public class TicketAfterEntryEndDateException extends ConflictBaseException {
  private static final long serialVersionUID = -7901986869675242406L;

  public TicketAfterEntryEndDateException(String message) {
    this.message = message;
  }

}
