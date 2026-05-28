package pl.hellopolandticket.service.exception.conflict;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class UsherEmailMatchesPartnerEmailException extends ConflictBaseException {
  private static final long serialVersionUID = 2175602176101336626L;
}
