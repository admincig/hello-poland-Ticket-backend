package pl.hellopolandticket.rest;

import java.util.Locale;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonbConfig implements ContextResolver<Jsonb> {

  private static Jsonb instance;

  public static Jsonb getInstance() {
    if (instance == null) {
      javax.json.bind.JsonbConfig config = new javax.json.bind.JsonbConfig()
          .withDateFormat("yyyy-MM-dd'T'HH:mm", new Locale("en", "GB"))
          .withAdapters(new LocalTimeCustomAdapter());
      instance = JsonbBuilder.create(config);
    }
    return instance;
  }

  @Override
  public Jsonb getContext(Class<?> type) {
    return getInstance();
  }

}
