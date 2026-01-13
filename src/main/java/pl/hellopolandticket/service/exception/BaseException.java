package pl.hellopolandticket.service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseException extends RuntimeException {
  private static final long serialVersionUID = -7539460397637784208L;

  protected String message;
  protected String errorKey;

  public String getErrorKey() { return errorKey; }
}
