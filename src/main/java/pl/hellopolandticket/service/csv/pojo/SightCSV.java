package pl.hellopolandticket.service.csv.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@JsonPropertyOrder(value = {"name", "lead", "description", "mainImageUrl", "email", "phone",
    "latitude", "longitude", "street", "zipCode", "city", "country", "userEmail"})
public class SightCSV implements Serializable {

  private static final long serialVersionUID = 5421767133423015831L;

  @NotNull
  private String name;

  private String lead;

  private String description;

  private String mainImageUrl;

  private String email;

  private String phone;

  private Double latitude;

  private Double longitude;

  private String street;

  private String zipCode;

  private String city;

  private String country;

  private String userEmail;

}