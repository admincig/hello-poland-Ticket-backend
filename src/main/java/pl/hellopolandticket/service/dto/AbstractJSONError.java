package pl.hellopolandticket.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AbstractJSONError {

  private Class<? extends Exception> exception;
  private String message;

  @Builder
  public AbstractJSONError(Class<? extends Exception> exception, String message) {
    this.exception = exception;
    this.message = message;
  }
}