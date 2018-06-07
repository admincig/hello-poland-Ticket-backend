package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class AbstractJSONError implements Serializable {

  private static final long serialVersionUID = 2681281975330282406L;

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