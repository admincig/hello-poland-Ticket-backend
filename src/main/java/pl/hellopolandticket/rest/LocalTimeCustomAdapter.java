package pl.hellopolandticket.rest;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.json.bind.adapter.JsonbAdapter;

public class LocalTimeCustomAdapter implements JsonbAdapter<LocalTime, String> {

  private final static DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("UTC"));

  @Override
  public String adaptToJson(LocalTime obj) throws Exception {
    return TIME_FORMATTER.format(obj);
  }

  @Override
  public LocalTime adaptFromJson(String obj) throws Exception {
    return LocalTime.parse(obj, TIME_FORMATTER);
  }

}
