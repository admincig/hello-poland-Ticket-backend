package pl.hellopolandticket.rest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ParamConverter;
import pl.hellopolandticket.annotation.DateFormat;
import pl.hellopolandticket.annotation.DateTimeFormat;

public class DateParameterConverter implements ParamConverter<Date> {

  public static final String DEFAULT_FORMAT = DateTimeFormat.DEFAULT_DATE_TIME_FORMAT;
  private DateTimeFormat customDateTimeFormat;
  private DateFormat customDateFormat;

  @Override
  public Date fromString(String value) {
    String format = DEFAULT_FORMAT;
    if (customDateFormat != null) {
      format = customDateFormat.value();
    } else if (customDateTimeFormat != null) {
      format = customDateTimeFormat.value();
    }

    final var simpleDateFormat = new SimpleDateFormat(format);
    try {
      return simpleDateFormat.parse(value);
    } catch (ParseException ex) {
      throw new WebApplicationException(ex);
    }
  }

  @Override
  public String toString(Date value) {
    var dateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
    return dateFormat.format(value);
  }

  public void setCustomDateTimeFormat(DateTimeFormat customDateTimeFormat) {
    this.customDateTimeFormat = customDateTimeFormat;
  }

  public void setCustomDateFormat(DateFormat customDateFormat) {
    this.customDateFormat = customDateFormat;
  }

}
