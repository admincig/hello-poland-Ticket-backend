package pl.hellopolandticket.service.exception.preconditionfailed;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NumberOfTicketsNotPositiveException extends PreconditionFailedBaseException {

  private static final long serialVersionUID = -5793398395778749990L;

}
