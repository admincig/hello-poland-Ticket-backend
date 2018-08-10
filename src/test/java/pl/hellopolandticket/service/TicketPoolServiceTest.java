package pl.hellopolandticket.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.FrequencyType;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionException;

public class TicketPoolServiceTest {

  @Test
  public void daily_period() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-02 11:00:00");
    Date expected = df.parse("2018-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void daily_single() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-02 10:00:00");
    Date expected = df.parse("2018-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void daily_single_frequency_2() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-06 10:00:00");
    Date expected = df.parse("2018-02-06 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_single_frequency_2() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-05 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_frequency_2_period_after_end_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-06 19:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_frequency_2_period_before_start_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-06 09:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test
  public void daily_single_frequency_4() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-02 10:00:00");
    Date expected = df.parse("2018-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_single_frequency_4() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-03 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 2, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_after_end_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-02 19:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_before_start_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-02 09:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_after_frequency_end_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2019-02-02 19:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void daily_invalid_before_frequency_start_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-10-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-10-01 10:00:00");
    Date endDate = df.parse("2018-10-01 18:00:00");
    Date requestDate = df.parse("2018-09-02 11:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.DAILY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test
  public void weekly_every_day() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-02 10:00:00");
    Date expected = df.parse("2018-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void weekly_invalid_every_day_after_frequency_end_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2019-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void weekly_invalid_every_day_before_frequency_start_date() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2017-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test
  public void weekly_every_day_period() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-02-02 11:00:00");
    Date expected = df.parse("2018-02-02 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void weekly_mondays() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-03-05 10:00:00");
    Date expected = df.parse("2018-03-05 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void weekly_period() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-05 18:00:00");
    Date requestDate = df.parse("2018-03-06 19:00:00");
    Date expected = df.parse("2018-03-05 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void weekly_period_through_weekend() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-04 10:00:00");
    Date endDate = df.parse("2018-01-08 18:00:00");
    Date requestDate = df.parse("2018-03-10 19:00:00");
    Date expected = df.parse("2018-03-08 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void weekly_invalid_period_through_weekend() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-04 10:00:00");
    Date endDate = df.parse("2018-01-08 18:00:00");
    Date requestDate = df.parse("2018-03-07 19:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test
  public void weekends_period() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-06 10:00:00");
    Date endDate = df.parse("2018-01-06 18:00:00");
    Date requestDate = df.parse("2018-08-12 13:00:00");
    Date expected = df.parse("2018-08-12 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKENDS, null, frequencyStartDate, frequencyEndDate);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void weekly_mondays_period() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 18:00:00");
    Date requestDate = df.parse("2018-03-05 13:10:00");
    Date expected = df.parse("2018-03-05 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test
  public void weekly_sundays() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-03-04 10:00:00");
    Date expected = df.parse("2018-03-04 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void weekly_invalid_mondays() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-03-06 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 1, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test(expected = CannotCreateTicketPoolForNotCyclicalPoolDefinitionException.class)
  public void weekly_invalid_frequency_4() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-07 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 4, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
  }

  @Test
  public void weekly_frequency_4() throws Exception {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Date frequencyStartDate = df.parse("2018-01-01 00:00:00");
    Date frequencyEndDate = df.parse("2018-12-31 23:59:59");
    Date startDate = df.parse("2018-01-01 10:00:00");
    Date endDate = df.parse("2018-01-01 10:00:00");
    Date requestDate = df.parse("2018-02-03 10:00:00");
    Date expected = df.parse("2018-02-03 10:00:00");

    FrequencyData fd =
        new FrequencyData(FrequencyType.WEEKLY, 4, frequencyStartDate, frequencyEndDate);
    List<Integer> days = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    fd.setDaysOfWeek(days);
    TicketPoolDefinition tpd = new TicketPoolDefinition();
    tpd.setFrequencyData(fd);
    tpd.setStartDate(startDate);
    tpd.setEndDate(endDate);

    Date result = new TicketPoolService().getStartDateForNewInstance(tpd, requestDate);
    Assert.assertEquals(expected, result);
  }

}
