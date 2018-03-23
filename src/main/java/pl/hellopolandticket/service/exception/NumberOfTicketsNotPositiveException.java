package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NumberOfTicketsNotPositiveException extends RuntimeException {

  private static final long serialVersionUID = -8680886096448021035L;
}
