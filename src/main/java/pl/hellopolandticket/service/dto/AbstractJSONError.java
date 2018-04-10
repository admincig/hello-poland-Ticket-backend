package pl.hellopolandticket.service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AbstractJSONError {

  private String exception;
  private String message;
  private Object object;

  @Builder
  public AbstractJSONError(Class<? extends Exception> exception, String message, Object object) {
    this.exception = exception.getSimpleName();
    this.message = message;
    this.object = object;
  }
}