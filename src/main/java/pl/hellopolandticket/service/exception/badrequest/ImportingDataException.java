package pl.hellopolandticket.service.exception.badrequest;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ImportingDataException extends BadRequestBaseException {

  private static final long serialVersionUID = 1033687284601061393L;

  public ImportingDataException(String message) {
    this.message = message;
  }
}
