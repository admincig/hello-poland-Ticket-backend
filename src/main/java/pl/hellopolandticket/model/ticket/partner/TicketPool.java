package pl.hellopolandticket.model.ticket.partner;

import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import static pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition.UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.preconditionfailed.NumberOfTicketsNotPositiveException;

@Getter
@Entity
@Table(name = "TICKET_POOLS")
@EqualsAndHashCode(exclude = "tickets")
@NoArgsConstructor
@ToString(exclude = {"tickets"})
public class TicketPool implements Serializable, Limited {

  private static final long serialVersionUID = -3301750425362262797L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TICKET_POOL_ID")
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
  @Column(name = "START_DATE")
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
  @ManyToOne(optional = false)
  @JoinColumn(name = "TICKET_POOL_DEFINITION_ID")
  private TicketPoolDefinition ticketPoolDefinition;

  @Setter
  @OneToMany(mappedBy = "ticketPool")
  private List<Ticket> tickets;

  @Setter
  @NotNull
  @Column(name = "WHOLEDAY", nullable = false)
  private boolean wholeDay;

  @Builder
  public TicketPool(TicketPoolDefinition parent, String name, Integer availableTicketsNumber,
      Date startDate, Date endDate, Date entryStartDate, Date entryEndDate, boolean wholeDay) {
    this.ticketPoolDefinition = parent;
    this.name = name;
    this.startDate = startDate;
    this.endDate = endDate;
    this.entryStartDate = entryStartDate != null ? entryStartDate : startDate;
    this.entryEndDate = entryEndDate != null ? entryEndDate : endDate;
    this.wholeDay = wholeDay;
    this.availableTicketsNumber =
        availableTicketsNumber == null ? UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE
            : availableTicketsNumber;
  }

  public TicketPool(TicketPoolDefinition parent) {
    this.ticketPoolDefinition = parent;
    this.name = parent.getName();
    this.availableTicketsNumber = parent.getAvailableTicketsNumber();
    this.endDate = parent.getEndDate();
    this.startDate = parent.getStartDate();
    this.entryStartDate = parent.getEntryStartDate();
    this.entryEndDate = parent.getEntryEndDate();
    this.wholeDay = parent.isWholeDay();
  }

  public boolean isEqualParent() {
    int availableTicketsNumber = this.availableTicketsNumber;
    if (this.tickets != null) {
      availableTicketsNumber += this.tickets.size();
    }

    // TODO fix equality for different frequency days
    return Objects.equals(this.name, ticketPoolDefinition.getName())
        && Objects.equals(availableTicketsNumber, ticketPoolDefinition.getAvailableTicketsNumber())
        && Objects.equals(this.startDate, ticketPoolDefinition.getStartDate())
        && Objects.equals(this.endDate, ticketPoolDefinition.getEndDate())
        && Objects.equals(this.entryStartDate, ticketPoolDefinition.getEntryStartDate())
        && Objects.equals(this.entryEndDate, ticketPoolDefinition.getEntryEndDate());
  }

  public void decreaseAvailableTicketsNumber(int numberOfTickets) {
    if (numberOfTickets <= 0) {
      throw new NumberOfTicketsNotPositiveException();
    }

    if (hasLimitedNumberOfTickets()) {
      if (hasEnoughTickets(numberOfTickets)) {
        availableTicketsNumber = availableTicketsNumber - numberOfTickets;
      } else {
        throw new NoAvailableTicketsException();
      }
    }
  }

  public void increaseAvailableTicketsNumber() {
    if (hasLimitedNumberOfTickets()) {
      availableTicketsNumber++;
    }
  }

  private boolean hasLimitedNumberOfTickets() {
    return availableTicketsNumber != UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }

  private boolean hasEnoughTickets(int numberOfTickets) {
    return availableTicketsNumber - numberOfTickets >= MIN_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
  }

  public void recountEntryDates() {
    Calendar startDateCal = Calendar.getInstance();
    startDateCal.setTime(startDate);
    Calendar cal = Calendar.getInstance();
    cal.setTime(entryStartDate);
    long duration =
        Duration.between(entryStartDate.toInstant(), entryEndDate.toInstant()).toMillis();
    cal.set(Calendar.DAY_OF_YEAR, startDateCal.get(Calendar.DAY_OF_YEAR));
    entryStartDate = cal.getTime();
    cal.add(Calendar.MILLISECOND, (int) duration);
    entryEndDate = cal.getTime();
  }

  public boolean isInFuture() {
    return this.startDate.after(new Date());
  }

  public int getBoughtTicketsCount() {
    return (int) getTickets().stream()
        .filter(t -> Status.BOUGHT.equals(t.getStatus()))
        .count();
  }

  @Override
  public int getTicketsLeftToBuyCount() {
    if (ticketPoolDefinition.getAvailableTicketsNumber() == -1) {
      return -1;
    }
    return Math.max(0, ticketPoolDefinition.getAvailableTicketsNumber() - getBoughtTicketsCount());
  }

}
