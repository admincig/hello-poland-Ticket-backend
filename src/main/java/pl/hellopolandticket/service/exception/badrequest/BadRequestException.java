package pl.hellopolandticket.service.exception.badrequest;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class BadRequestException extends BadRequestBaseException {
  private static final long serialVersionUID = -725500820089501452L;

  public BadRequestException(String message) {
    this.message = message;
  }
}
