package pl.hellopolandticket.service.exception.notfound;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ResourceNotFoundException extends NotFoundBaseException {

  private static final long serialVersionUID = 1657726507596909599L;
}
