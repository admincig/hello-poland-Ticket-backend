package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CannotGenerateQrCodeException extends RuntimeException {

  private static final long serialVersionUID = -1985611764816226363L;
}