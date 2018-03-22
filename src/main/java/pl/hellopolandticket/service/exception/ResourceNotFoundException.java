package pl.hellopolandticket.service.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = -2773652062517959781L;
}
