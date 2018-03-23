package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ImportingDataException extends RuntimeException {

  private static final long serialVersionUID = -1629042231220601668L;
}
