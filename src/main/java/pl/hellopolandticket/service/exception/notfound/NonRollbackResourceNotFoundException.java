package pl.hellopolandticket.service.exception.notfound;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = false)
public class NonRollbackResourceNotFoundException extends NotFoundBaseException {

  private static final long serialVersionUID = 1657726507596909599L;

  public NonRollbackResourceNotFoundException(String message) {
    this.message = message;
  }
}
