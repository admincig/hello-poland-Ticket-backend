package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException;
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionRollbackException;

@Stateless
@LocalBean
public class TicketPoolService extends ServiceSuperclass {

  @Inject
  private TicketPoolDao ticketPoolDao;
  @Inject
  private AvailableTicketNumberAssociationService atnaService;

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketPool find(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    return ticketPoolDao.find(ticketPoolDefinition, requestedDate);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketPool createNew(TicketPoolDefinition ticketPoolDefinition, Date requestedDate) {
    Date startDate = null;
    try {
      startDate = getStartDateForNewInstance(ticketPoolDefinition, requestedDate);
    } catch (CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException e) {
      logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionRollbackException(
          "Ządana data poza zakresem definicji puli");
    }
    Date endDate = getEndDateForNewInstance(ticketPoolDefinition, startDate);
    var pool = new TicketPool(ticketPoolDefinition);
    pool.setStartDate(startDate);
    pool.setEndDate(endDate);
    ticketPoolDao.persist(pool);
    pool.recountEntryDates();
    atnaService.add(pool, ticketPoolDefinition);
    return pool;
  }

  private Date getEndDateForNewInstance(TicketPoolDefinition ticketPoolDefinition,
      Date requestedDate) {
    int millisBetween = (int) Duration.between(ticketPoolDefinition.getStartDate().toInstant(),
        ticketPoolDefinition.getEndDate().toInstant()).toMillis();
    Calendar cal = Calendar.getInstance();
    cal.setTime(requestedDate);
    cal.add(Calendar.MILLISECOND, millisBetween);
    return cal.getTime();
  }

  private Date getStartDateForNewInstance(TicketPoolDefinition ticketPoolDefinition,
      Date requestedDate)
      throws CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException {
    if (ticketPoolDefinition.getIsCyclic()) {
      return getStartDateForNewInstanceOfCyclicPool(ticketPoolDefinition, requestedDate);
    } else if (ticketPoolDefinition.getStartDate().equals(requestedDate)) {
      return ticketPoolDefinition.getStartDate();
    }
    throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
        "Ządana data poza zakresem definicji puli");

  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<LocalDate> getAllStartDatesForInstancesOfCyclicPool(Long tpdId,
      LocalDate dateFrom, LocalDate dateTo) {
    TicketPoolDefinition tpd = em.find(TicketPoolDefinition.class, tpdId);
    @SuppressWarnings("deprecation")
    LocalTime hour = LocalTime.of(tpd.getStartDate().getHours(), tpd.getStartDate().getMinutes());
    return dateFrom.datesUntil(dateTo).map(dateIter -> {
      try {
        Date date = getStartDateForNewInstance(tpd, Date.from(dateIter.atTime(hour)
            .atZone(ZoneId.systemDefault())
            .toInstant()));
        return LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
      } catch (Exception e) {
        logger.log(Level.WARNING, "nope: " + e.getMessage());
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.toList());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Date getStartDateForNewInstanceOfCyclicPool(TicketPoolDefinition ticketPoolDefinition,
      Date requestedDate)
      throws CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException {
    if (requestedDate.before(ticketPoolDefinition.getStartDate())) {
      logger.log(Level.DEBUG, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
          "Ządana data jest poza zakresem definicji puli");
    }
    if (ticketPoolDefinition.getFrequencyData().getEndDate() != null
        && requestedDate.after(ticketPoolDefinition.getFrequencyData().getEndDate())) {
      logger.log(Level.DEBUG, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
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
      case MONTHLY:
        return getMonthlyStartDate(ticketPoolDefinition, requestedDate);
      default:
        return null;
    }
  }

  private Date getMonthlyStartDate(TicketPoolDefinition ticketPoolDefinition, Date requestedDate)
      throws CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException {
    FrequencyData frequencyData = ticketPoolDefinition.getFrequencyData();
    Date startDate = ticketPoolDefinition.getStartDate();
    Calendar startDateCal = Calendar.getInstance();
    startDateCal.setTime(startDate);
    Date endDate = ticketPoolDefinition.getEndDate();
    Duration durationBetweenStartDateAndRequestDate =
        Duration.between(startDateCal.toInstant(), requestedDate.toInstant());
    long monthsBetween = durationBetweenStartDateAndRequestDate.toDays() / 30;
    double divide = 1.0 * monthsBetween / ticketPoolDefinition.getFrequencyData().getFrequency();
    if (divide - (int) divide > 0.01
        || dateTimeNotInMonthlyRange(requestedDate, startDate, endDate)) {
      logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
          "Ządana data jest poza zakresem definicji puli");
    }
    int daysOfMonthDifference = 0;
    if (frequencyData.getDaysOfMonth() != null && !frequencyData.getDaysOfMonth().isEmpty()) {
      LocalDate ld = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int dom = ld.getDayOfMonth();
      if (!frequencyData.getDaysOfMonth().contains(dom)) {
        logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate
            + ", id=" + ticketPoolDefinition.getId() + "]");
        throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
            "Ządana data jest poza zakresem definicji puli");
      }
    } else {
      daysOfMonthDifference = (int) (durationBetweenStartDateAndRequestDate.toDays() % 30);
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(requestedDate);
    cal.set(Calendar.HOUR_OF_DAY, startDateCal.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, startDateCal.get(Calendar.MINUTE));
    cal.add(Calendar.DAY_OF_YEAR, -daysOfMonthDifference);
    return cal.getTime();
  }

  private Date getWeeklyStartDate(TicketPoolDefinition ticketPoolDefinition, Date requestedDate)
      throws CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException {
    FrequencyData frequencyData = ticketPoolDefinition.getFrequencyData();
    Date startDate = ticketPoolDefinition.getStartDate();
    Calendar startDateCal = Calendar.getInstance();
    startDateCal.setTime(startDate);
    Date endDate = ticketPoolDefinition.getEndDate();
    Duration durationBetweenStartDateAndRequestDate =
        Duration.between(startDateCal.toInstant(), requestedDate.toInstant());
    long weeksBetween = durationBetweenStartDateAndRequestDate.toDays() / 7;
    double divide = 1.0 * weeksBetween / ticketPoolDefinition.getFrequencyData().getFrequency();
    if (divide - (int) divide > 0.01
        || dateTimeNotInWeeklyRange(requestedDate, startDate, endDate)) {
      logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
          "Ządana data jest poza zakresem definicji puli");
    }
    int daysOfWeekDifference = 0;
    if (frequencyData.getDaysOfWeek() != null && !frequencyData.getDaysOfWeek().isEmpty()) {
      LocalDate ld = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int dow = ld.getDayOfWeek().getValue();
      if (!frequencyData.getDaysOfWeek().contains(dow)) {
        logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate
            + ", id=" + ticketPoolDefinition.getId() + "]");
        throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
            "Ządana data jest poza zakresem definicji puli");
      }
    } else {
      daysOfWeekDifference = (int) (durationBetweenStartDateAndRequestDate.toDays() % 7);
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(requestedDate);
    cal.set(Calendar.HOUR_OF_DAY, startDateCal.get(Calendar.HOUR_OF_DAY));
    cal.set(Calendar.MINUTE, startDateCal.get(Calendar.MINUTE));
    cal.add(Calendar.DAY_OF_YEAR, -daysOfWeekDifference);
    return cal.getTime();
  }

  private boolean dateTimeNotInWeeklyRange(Date requestedDate, Date startDate, Date endDate) {
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

  private boolean dateTimeNotInMonthlyRange(Date requestedDate, Date startDate, Date endDate) {
    LocalDateTime slt = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    LocalDateTime elt = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    if (Duration.between(slt, elt).toDays() > 0) { // period
      LocalDateTime rlt =
          requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      int startDay = slt.getDayOfMonth();
      int endDay = elt.getDayOfMonth();
      int requestedDay = rlt.getDayOfMonth();
      if (startDay > endDay) {
        endDay += 30;
      }
      return requestedDay < startDay || requestedDay > endDay
          || (requestedDay == startDay ? rlt.toLocalTime().isBefore(slt.toLocalTime())
              : requestedDay == endDay ? rlt.toLocalTime().isAfter(elt.toLocalTime()) : false);
    } else { // single day
      return timeNotInRange(requestedDate, startDate, endDate);
    }
  }

  private Date getDailyStartDate(TicketPoolDefinition ticketPoolDefinition, Date requestedDate)
      throws CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException {
    Date startDate = ticketPoolDefinition.getStartDate();
    Date endDate = ticketPoolDefinition.getEndDate();

    Duration durationBetweenStartDateAndRequestDate =
        Duration.between(startDate.toInstant(), requestedDate.toInstant());
    double divide = 1.0 * durationBetweenStartDateAndRequestDate.toDays()
        / ticketPoolDefinition.getFrequencyData().getFrequency();

    if (divide - (int) divide > 0.01 || timeNotInRange(requestedDate, startDate, endDate)) {
      logger.log(Level.ERROR, "Ządana data poza zakresem definicji puli [" + requestedDate + ", id="
          + ticketPoolDefinition.getId() + "]");
      throw new CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException(
          "Ządana data jest poza zakresem definicji puli");
    }
    Calendar cal = Calendar.getInstance();
    cal.setTime(startDate);
    Calendar reqCal = Calendar.getInstance();
    reqCal.setTime(requestedDate);
    reqCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
    reqCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
    return reqCal.getTime();
  }

  private boolean timeNotInRange(Date requestedDate, Date startDate, Date endDate) {
    LocalTime rlt = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    LocalTime slt = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    LocalTime elt = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

    LocalDate rld = requestedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate sld = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate eld = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    if (sld.isBefore(eld) && rld.isEqual(sld)) {
      return rlt.isBefore(slt) || rlt.isBefore(elt);
    }
    if (sld.isBefore(eld) && rld.isEqual(eld)) {
      return rlt.isAfter(elt);
    }
    return rlt.isBefore(slt) || rlt.isAfter(elt);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<TicketPool> updatePoolsAvailableTicketsNumber(TicketPoolDefinition tpd,
      Integer oldAvailableTicketsNumber) {
    List<TicketPool> pools = tpd.getTicketPools();
    return pools.stream()
        .filter(TicketPool::isInFuture)
        .map(pool -> {
          pool.setName(tpd.getName());
          if (tpd.getAvailableTicketsNumber() == -1) {
            pool.setAvailableTicketsNumber(-1);
          } else {
            int booked = oldAvailableTicketsNumber - pool.getAvailableTicketsNumber();
            pool.setAvailableTicketsNumber(Math.max(0, tpd.getAvailableTicketsNumber() - booked));
          }
          return pool;
        }).collect(Collectors.toList());
  }

}
