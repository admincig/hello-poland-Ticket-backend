package pl.hellopolandticket.model.ticket.partner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.TicketDefWithAtna;

@Getter
@Entity
@Table(name = "TICKET_POOL_DEFINITIONS")
@NoArgsConstructor
@ToString(exclude = {"atnas", "ticketPools"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TicketPoolDefinition implements Serializable {

  private static final long serialVersionUID = 8904209837208814831L;

  public static final int UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE = -1;

  public static final int MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_POOL_DEFINITION_ID")
  @EqualsAndHashCode.Include
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @NotNull
  @Column(name = "AVAILABLE_TICKETS_NUMBER", nullable = false)
  private Integer availableTicketsNumber;

  @Setter
  @NotNull
  @Column(name = "IS_CYCLIC", nullable = false)
  private Boolean isCyclic;

  @Setter
  @Embedded
  private FrequencyData frequencyData;

  @Setter
  @NotNull
  @Column(name = "START_DATE", nullable = false)
  private Date startDate;

  @Setter
  @Column(name = "END_DATE")
  private Date endDate;

  @Setter
  @Column(name = "ENTRY_START_DATE")
  private Date entryStartDate;

  @Setter
  @Column(name = "ENTRY_END_DATE")
  private Date entryEndDate;

  @Setter
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SIGHT_EVENT_ID", nullable = false)
  private SightEvent sightEvent;

  @Setter
  @OneToMany(mappedBy = "ticketPoolDefinition")
  private List<AvailableTicketNumberAssociation> atnas = new ArrayList<>();

  @Setter
  @OneToMany(mappedBy = "ticketPoolDefinition")
  @OrderBy("id desc")
  private List<TicketPool> ticketPools;

  @Setter
  @NotNull
  @Column(name = "DELETED", nullable = false)
  private boolean deleted;

  @Setter
  @NotNull
  @Column(name = "WHOLEDAY", nullable = false)
  private boolean wholeDay;


    public List<TicketDefinition> getTicketDefinitions() {
        return atnas.stream()
                .map(AvailableTicketNumberAssociation::getTicketDefinition)
                .toList();
    }

  public List<AvailableTicketNumberAssociation> getUndeletedAtnas() {
    return this.getAtnas().stream().filter(atna -> !atna.isDeleted()).collect(Collectors.toList());
  }

    public List<TicketDefWithAtna> getTicketDefsWithAtna() {
        return atnas.stream()
                .filter(atna -> !atna.isDeleted())
                .map(atna -> new TicketDefWithAtna(
                        atna.getTicketDefinition(),
                        atna
                ))
                .toList();
    }


    @Builder
  public TicketPoolDefinition(String name, Integer availableTicketsNumber, Boolean isCyclic,
      FrequencyData frequencyData, Date startDate, Date endDate, Date entryStartDate,
      Date entryEndDate, SightEvent sightEvent, boolean deleted, boolean wholeDay) {
    this.name = name;
    this.isCyclic = isCyclic;
    this.frequencyData = frequencyData;
    this.startDate = startDate;
    this.endDate = endDate;
    this.entryStartDate = entryStartDate != null ? entryStartDate : startDate;
    this.entryEndDate = entryEndDate != null ? entryEndDate : endDate;
    this.sightEvent = sightEvent;
    this.deleted = deleted;
    this.wholeDay = wholeDay;
    this.availableTicketsNumber =
        availableTicketsNumber == null ? UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE
            : availableTicketsNumber;
  }

  private boolean hasAllAtnasDeleted() {
    return getAtnas().stream().allMatch(atna -> atna.isDeleted());
  }

  public boolean isNotDeletedAndHasNoAtnas() {
    return !isDeleted() && hasAllAtnasDeleted();
  }

}
