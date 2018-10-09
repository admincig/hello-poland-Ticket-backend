package pl.hellopolandticket.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.bind.adapter.JsonbAdapter;

public class DateCustomAdapter implements JsonbAdapter<Date, String> {

  private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  @Override
  public String adaptToJson(Date obj) throws Exception {
    return DATE_FORMAT.format(obj);
  }

  @Override
  public Date adaptFromJson(String obj) throws Exception {
    return DATE_FORMAT.parse(obj);
  }

}
