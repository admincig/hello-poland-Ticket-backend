package pl.hellopolandticket.model.ticket.partner;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.sightevent.SightEvent;

@Getter
@Entity
@Table(name = "TICKET_POOL_DEFINITIONS")
@EqualsAndHashCode(exclude = {"ticketDefinitions", "ticketPools"})
@NoArgsConstructor
@ToString(exclude = {"ticketDefinitions", "ticketPools"})
public class TicketPoolDefinition implements Serializable {

  private static final long serialVersionUID = 8904209837208814831L;

  public static final int UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE = -1;

  public static final int MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_POOL_DEFINITION_ID")
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
  @ManyToOne
  @JoinColumn(name = "SIGHT_EVENT_ID", nullable = false)
  private SightEvent sightEvent;

  @Setter
  @ManyToMany(mappedBy = "ticketPoolDefinitions")
  @JoinTable(name = "TICKET_POOL_DEFINITIONS_TICKET_DEFINITIONS",
      joinColumns = {@JoinColumn(name = "TICKET_POOL_DEFINITION_ID")},
      inverseJoinColumns = {@JoinColumn(name = "TICKET_DEFINITION_ID")})
  private List<TicketDefinition> ticketDefinitions = new ArrayList<>();

  @Setter
  @OneToMany(mappedBy = "ticketPoolDefinition")
  @OrderBy("id desc")
  private List<TicketPool> ticketPools;

  @Setter
  @NotNull
  @Column(name = "DELETED", nullable = false)
  private boolean deleted;

  @Builder
  public TicketPoolDefinition(String name, Integer availableTicketsNumber, Boolean isCyclic,
      FrequencyData frequencyData, Date startDate, Date endDate, Date entryStartDate,
      Date entryEndDate, SightEvent sightEvent, boolean deleted) {
    this.name = name;
    this.isCyclic = isCyclic;
    this.frequencyData = frequencyData;
    this.startDate = startDate;
    this.endDate = endDate;
    this.entryStartDate = entryStartDate != null ? entryStartDate : startDate;
    this.entryEndDate = entryEndDate != null ? entryEndDate : endDate;
    this.sightEvent = sightEvent;
    this.deleted = deleted;
    this.availableTicketsNumber =
        availableTicketsNumber == null ? UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE
            : availableTicketsNumber;
  }

}
