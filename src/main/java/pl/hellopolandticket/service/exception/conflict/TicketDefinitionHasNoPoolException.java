package pl.hellopolandticket.service.exception.conflict;

public class TicketDefinitionHasNoPoolException extends ConflictBaseException {
  private static final long serialVersionUID = 1638921201507993743L;

  public TicketDefinitionHasNoPoolException(String message) {
    this.message = message;
  }

}
