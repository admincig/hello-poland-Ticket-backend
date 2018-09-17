package pl.hellopolandticket.model.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "APPLICATION_PROPERTIES")
@EqualsAndHashCode
@NoArgsConstructor
@ToString(exclude = "propertyValue")
public class ApplicationProperty {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "APPLICATION_PROPERTY_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "PROPERTY_NAME", nullable = false, unique = true)
  private String propertyName;

  @Setter
  @Column(name = "PROPERTY_VALUE")
  private String propertyValue;

  @Builder
  public ApplicationProperty(String propertyName, String propertyValue) {
    this.propertyName = propertyName;
    this.propertyValue = propertyValue;
  }
}
