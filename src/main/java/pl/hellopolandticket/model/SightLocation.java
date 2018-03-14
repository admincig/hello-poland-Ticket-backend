package pl.hellopolandticket.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class SightLocation implements Serializable {

  private static final long serialVersionUID = 1599586813368109778L;

  @Setter
  @Column(name = "LATITUDE")
  private Double latitude;

  @Setter
  @Column(name = "LONGITUDE")
  private Double longitude;

  @Setter
  @Column(name = "STREET")
  private String street;

  @Setter
  @Column(name = "ZIP_CODE")
  private String zipCode;

  @Setter
  @Column(name = "CITY")
  private String city;

  @Setter
  @Column(name = "COUNTRY")
  private String country;

  @Builder
  public SightLocation(Double latitude, Double longitude, String street, String zipCode,
      String city, String country) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.street = street;
    this.zipCode = zipCode;
    this.city = city;
    this.country = country;
  }
}