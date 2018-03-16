package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class SendingHttpRequestException extends RuntimeException {

  private static final long serialVersionUID = -7286774362265923537L;
}
