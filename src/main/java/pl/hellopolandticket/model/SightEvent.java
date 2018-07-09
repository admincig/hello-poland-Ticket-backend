package pl.hellopolandticket.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "SIGHT_EVENTS")
@EqualsAndHashCode(exclude = {"ticketDefinitions"})
@NoArgsConstructor
@ToString(exclude = "ticketDefinitions")
public class SightEvent implements Serializable {

  private static final long serialVersionUID = 5345966403908441388L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "SIGHT_EVENT_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @Column(name = "DATE")
  private Date date;

  @Setter
  @Column(name = "DESCRIPTION")
  private String description;

  @Setter
  @Column(name = "DURATION")
  private Integer duration;

  @Setter
  @Column(name = "MAIN_IMAGE_URL")
  private String mainImageUrl;

  @Setter
  @Column(name = "EMAIL")
  private String email;

  @Setter
  @Column(name = "PHONE")
  private String phone;

  @Setter
  @Embedded
  private SightLocation sightLocation;

  @Setter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "PARTNER_ID", nullable = false)
  private Partner partner;

  @Setter
  @OneToMany(mappedBy = "sightEvent")
  private List<TicketDefinition> ticketDefinitions = new ArrayList<>();

  @Setter
  @NotNull
  private Boolean active = true;

  @Setter
  @NotNull
  private Boolean generalAdmission;

  @Builder
  public SightEvent(String name, Date date, String description,
      Integer duration, String mainImageUrl, String email, String phone,
      SightLocation sightLocation, Partner partner, Boolean generalAdmission) {
    this.name = name;
    this.date = date;
    this.description = description;
    this.duration = duration;
    this.mainImageUrl = mainImageUrl;
    this.email = email;
    this.phone = phone;
    this.sightLocation = sightLocation;
    this.partner = partner;
    this.generalAdmission = generalAdmission;
  }

}
