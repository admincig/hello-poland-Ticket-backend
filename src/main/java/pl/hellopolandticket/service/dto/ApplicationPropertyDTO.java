package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.ApplicationProperty;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationPropertyDTO implements Serializable {

  private static final long serialVersionUID = 6389090907393901615L;

  private String propertyName;

  private String propertyValue;

  public static ApplicationPropertyDTO ofApplicationProperty(
      ApplicationProperty applicationProperty) {
    return ApplicationPropertyDTO.builder()
        .propertyName(applicationProperty.getPropertyName())
        .propertyValue(applicationProperty.getPropertyValue())
        .build();
  }
}