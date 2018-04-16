package pl.hellopolandticket.service.exception.badrequest;

import javax.ejb.ApplicationException;

@ApplicationException
public class EmailSendingException extends BadRequestBaseException {

  private static final long serialVersionUID = -2560393505224706L;
}
