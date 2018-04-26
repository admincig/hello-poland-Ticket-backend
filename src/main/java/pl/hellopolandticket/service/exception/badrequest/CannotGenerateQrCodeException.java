package pl.hellopolandticket.service.exception.badrequest;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CannotGenerateQrCodeException extends BadRequestBaseException {

  private static final long serialVersionUID = 4385651506864364513L;

}