package pl.hellopolandticket.service.dto;

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
public class ApplicationPropertyDTO {

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