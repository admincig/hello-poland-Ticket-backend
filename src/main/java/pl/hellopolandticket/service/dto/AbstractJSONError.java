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

  @Builder
  public AbstractJSONError(Class<? extends Exception> exception, String message) {
    this.exception = exception.getName();
    this.message = message;
  }
}