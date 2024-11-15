package pl.hellopolandticket.service.exception.conflict;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class EventDoesNotTakePlaceOnChosenDateException extends ConflictBaseException {

  private static final long serialVersionUID = -7183715046441118822L;

  public EventDoesNotTakePlaceOnChosenDateException(String message) {
    this.message = message;
  }
}
