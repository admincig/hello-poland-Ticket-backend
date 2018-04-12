package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class EmailSendingException extends RuntimeException {

  private static final long serialVersionUID = 3365728416026664442L;
}
