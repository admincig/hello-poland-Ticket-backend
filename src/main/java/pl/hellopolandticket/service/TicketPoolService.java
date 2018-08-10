package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketPool;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketPoolDTO;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionException;

@Stateless
@LocalBean
public class TicketPoolService extends ServiceSuperclass {

  @Inject
  private TicketPoolDao ticketPoolDao;

  @Inject
  private TicketDao ticketDao;

  public TicketPool findOrCreateNew(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    TicketPool pool = ticketPoolDao.find(ticketPoolDefinition, requestedDate);
    if (pool == null) {
      Date startDate = getStartDateForNewInstance(ticketPoolDefinition, requestedDate);
      Date endDate = getEndDateForNewInstance(ticketPoolDefinition, startDate);
      // validateCreatingTicketPoolInstanceIsPossible(ticketPoolDefinition, requestedDate);
      pool = new TicketPool(ticketPoolDefinition);
      pool.setStartDate(startDate);
      pool.setEndDate(endDate);
      ticketPoolDao.persist(pool);
    }

    return pool;
  }

  private Date getEndDateForNewInstance(TicketPoolDefinition ticketPoolDefinition,
      Date requestedDate) {
    int hoursBetween = (int) Duration.between(ticketPoolDefinition.getStartDate().toInstant(),
        ticketPoolDefinition.getEndDate().toInstant()).toHours();
    Calendar cal = Calendar.getInstance();
    cal.setTime(requestedDate);
    cal.add(Calendar.HOUR, hoursBetween);
    return cal.getTime();
  }

  Date getStartDateForNewInstance(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    if (requestedDate.after(ticketPoolDefinition.getFrequencyData().getEndDate())
        || requestedDate.before(ticketPoolDefinition.getFrequencyData().getStartDate())) {
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionException(
          "Ządana data jest poza zakresem definicji puli");
    }
    switch (ticketPoolDefinition.getFrequencyData().getFrequencyType()) {
      case DAILY:
        return getDailyStartDate(ticketPoolDefinition, requestedDate);
      case WEEKDAYS:
        ticketPoolDefinition.getFrequencyData().setDaysOfWeek(Arrays.asList(1, 2, 3, 4, 5));
        ticketPoolDefinition.getFrequencyData().setFrequency(1);
        return getWeeklyStartDate(ticketPoolDefinition, requestedDate);
      case WEEKENDS:
        ticketPoolDefinition.getFrequencyData().setDaysOfWeek(Arrays.asList(6, 7));
        ticketPoolDefinition.getFrequencyData().setFrequency(1);
        return getWeeklyStartDate(ticketPoolDefinition, requestedDate);
      case WEEKLY:
        return getWeeklyStartDate(ticketPoolDefinition, requestedDate);
      default:
        return null;
    }
  }

  @SuppressWarnings("deprecation")
  private Date getWeeklyStartDate(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    FrequencyData frequencyData = ticketPoolDefinition.getFrequencyData();
    Date startDate = ticketPoolDefinition.getStartDate();
    Date endDate = ticketPoolDefinition.getEndDate();

    Duration durationBetweenStartDateAndRequestDate =
        Duration.between(startDate.toInstant(), requestedDate.toInstant());
    long weeksBetween = durationBetweenStartDateAndRequestDate.toDays() / 7;
    double divide = 1.0 * weeksBetween / ticketPoolDefinition.getFrequencyData().getFrequency();
    if (divide - (int) divide > 0.01 || dateTimeNotInRange(requestedDate, startDate, endDate)) {
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionException(
          "Ządana data jest poza zakresem definicji puli");
    }
    int daysOfWeekDifference = 0;
    if (frequencyData.getDaysOfWeek() != null && !frequencyData.getDaysOfWeek().isEmpty()) {
      LocalDate ld = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int dow = ld.getDayOfWeek().getValue();
      if (!frequencyData.getDaysOfWeek().contains(dow)) {
        throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionException(
            "Ządana data jest poza zakresem definicji puli");
      }
    } else {
      daysOfWeekDifference = (int) (durationBetweenStartDateAndRequestDate.toDays() % 7);
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(requestedDate);
    cal.set(Calendar.HOUR, startDate.getHours());
    cal.set(Calendar.MINUTE, startDate.getMinutes());
    cal.set(Calendar.SECOND, startDate.getSeconds());
    cal.add(Calendar.DAY_OF_YEAR, -daysOfWeekDifference);
    return cal.getTime();
  }

  private boolean dateTimeNotInRange(Date requestedDate, Date startDate, Date endDate) {

    LocalDateTime slt = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime elt = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    if (Duration.between(slt, elt).toDays() > 0) { // period
      LocalDateTime rlt =
          requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      int startDay = slt.getDayOfWeek().getValue();
      int endDay = elt.getDayOfWeek().getValue();
      int requestedDay = rlt.getDayOfWeek().getValue();
      if (startDay > endDay) {
        endDay += 7;
      }
      return requestedDay < startDay || requestedDay > endDay
          || (requestedDay == startDay ? rlt.toLocalTime().isBefore(slt.toLocalTime())
              : requestedDay == endDay ? rlt.toLocalTime().isAfter(elt.toLocalTime()) : false);
    } else { // single day
      return timeNotInRange(requestedDate, startDate, endDate);
    }
  }

  @SuppressWarnings("deprecation")
  private Date getDailyStartDate(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    Date startDate = ticketPoolDefinition.getStartDate();
    Date endDate = ticketPoolDefinition.getEndDate();

    Duration durationBetweenStartDateAndRequestDate =
        Duration.between(startDate.toInstant(), requestedDate.toInstant());
    double divide = 1.0 * durationBetweenStartDateAndRequestDate.toDays()
        / ticketPoolDefinition.getFrequencyData().getFrequency();

    if (divide - (int) divide > 0.01 || timeNotInRange(requestedDate, startDate, endDate)) {
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionException(
          "Ządana data jest poza zakresem definicji puli");
    }

    requestedDate.setHours(startDate.getHours());
    requestedDate.setMinutes(startDate.getMinutes());
    requestedDate.setSeconds(startDate.getSeconds());
    return requestedDate;
  }

  private boolean timeNotInRange(Date requestedDate, Date startDate, Date endDate) {
    LocalTime rlt = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    LocalTime slt = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    LocalTime elt = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    return rlt.isBefore(slt) || rlt.isAfter(elt);
  }

  private TicketPoolDTO toTicketPoolDTO(TicketPool ticketPool) {
    int totalTicketsNumber =
        ticketDao.countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(ticketPool.getId(),
            asList(BOUGHT, PUNCHED)).intValue();

    int boughtTicketsNumber =
        ticketDao.countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(ticketPool.getId(),
            singletonList(BOUGHT)).intValue();

    TicketPoolDTO ticketPoolDTO = ofTicketPool(ticketPool);

    ticketPoolDTO.totalTicketsNumber = totalTicketsNumber;
    ticketPoolDTO.boughtTicketNumber = boughtTicketsNumber;

    return ticketPoolDTO;
  }

}
