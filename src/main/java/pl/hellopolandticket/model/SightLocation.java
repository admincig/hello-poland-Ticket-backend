package pl.hellopolandticket.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@ToString
public class SightLocation implements Serializable {

  private static final long serialVersionUID = 1599586813368109778L;

  @Column(name = "LATITUDE")
  private Double latitude;

  @Column(name = "LONGITUDE")
  private Double longitude;

  @Column(name = "STREET")
  private String street;

  @Column(name = "ZIP_CODE")
  private String zipCode;

  @Column(name = "CITY")
  private String city;

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