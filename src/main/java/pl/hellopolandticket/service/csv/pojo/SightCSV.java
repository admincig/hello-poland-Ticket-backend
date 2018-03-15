package pl.hellopolandticket.service.csv.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.SightLocation;

@Setter
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@JsonPropertyOrder(value = {"name", "lead", "description", "mainImageUrl", "email", "latitude",
    "longitude", "street", "zipCode", "city", "country"})
public class SightCSV implements Serializable {

  private static final long serialVersionUID = 5421767133423015831L;

  @NotNull
  private String name;

  private String lead;

  private String description;

  private String mainImageUrl;

  private String email;

  private Double latitude;

  private Double longitude;

  private String street;

  private String zipCode;

  private String city;

  private String country;

  @PostConstruct
  public void validate() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    validator.validate(this);
  }

  public Sight createSight() {
    SightLocation sightLocation = SightLocation.builder()
        .latitude(latitude)
        .longitude(longitude)
        .street(street)
        .zipCode(zipCode)
        .city(city)
        .country(country)
        .build();

    return Sight.builder()
        .name(name)
        .lead(lead)
        .description(description)
        .mainImageUrl(mainImageUrl)
        .email(email)
        .sightLocation(sightLocation)
        .build();
  }
}