package pl.hellopolandticket.model.sightevent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Getter
@Entity
@Table(name = "SIGHT_EVENTS")
@NoArgsConstructor
@ToString(exclude = {"ticketPoolDefinitions", "openingHours", "pdfAttachmentsPaths"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SightEvent implements Serializable {

  private static final long serialVersionUID = 5345966403908441388L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "SIGHT_EVENT_ID")
  @EqualsAndHashCode.Include
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @Column(name = "DESCRIPTION", length = 2500)
  private String description;

  @Setter
  @Column(name = "LEAD")
  private String lead;

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
  private List<OpeningHours> openingHours = new ArrayList<>();

  @Setter
  @NotNull
  @Column(nullable = false)
  private Boolean published = false;

  @Setter
  @NotNull
  @Column(nullable = false)
  private Boolean blocked = false;

  @Setter
  @ElementCollection(fetch = FetchType.LAZY)
  private Set<PdfAttachment> pdfAttachmentsPaths;

  @Builder
  public SightEvent(String name, Date date, String description, String lead, Integer duration,
      String mainImageUrl, String email, String phone, SightEventLocation sightEventLocation,
      Partner partner, Boolean generalAdmission, Boolean published, Boolean blocked) {
    this.name = name;
    this.description = description;
    this.lead = lead;
    this.duration = duration;
    this.mainImageUrl = mainImageUrl;
    this.email = email;
    this.phone = phone;
    this.sightEventLocation = sightEventLocation;
    this.partner = partner;
    this.generalAdmission = generalAdmission;
    this.blocked = blocked != null ? blocked : false;
    this.published = published != null ? published : false;
  }

}
