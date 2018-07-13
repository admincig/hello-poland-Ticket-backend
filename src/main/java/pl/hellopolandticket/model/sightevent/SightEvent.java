package pl.hellopolandticket.model.sightevent;

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
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Getter
@Entity
@Table(name = "SIGHT_EVENTS")
@EqualsAndHashCode(exclude = {"ticketPoolDefinitions", "ticketPools"})
@NoArgsConstructor
@ToString(exclude = {"ticketPoolDefinitions", "ticketPools"})
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
  private SightEventLocation sightEventLocation;

  @Setter
  @NotNull
  @ManyToOne
  @JoinColumn(name = "PARTNER_ID", nullable = false)
  private Partner partner;

  @Setter
  @NotNull
  @Column(name = "ACTIVE", nullable = false)
  private Boolean active = true;

  @Setter
  @NotNull
  @Column(name = "GENERAL_ADMISSION", nullable = false)
  private Boolean generalAdmission;

  @Setter
  @OneToMany(mappedBy = "sightEvent")
  private List<TicketPoolDefinition> ticketPoolDefinitions = new ArrayList<>();

  @Setter
  @OneToMany(mappedBy = "sightEvent")
  private List<TicketPool> ticketPools = new ArrayList<>();

  @Builder
  public SightEvent(String name, Date date, String description,
      Integer duration, String mainImageUrl, String email, String phone,
      SightEventLocation sightEventLocation, Partner partner, Boolean generalAdmission) {
    this.name = name;
    this.description = description;
    this.duration = duration;
    this.mainImageUrl = mainImageUrl;
    this.email = email;
    this.phone = phone;
    this.sightEventLocation = sightEventLocation;
    this.partner = partner;
    this.generalAdmission = generalAdmission;
  }

}
