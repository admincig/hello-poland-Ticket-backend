package pl.hellopolandticket.rest;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonbConfig implements ContextResolver<Jsonb> {

  private static Jsonb instance;

  public static Jsonb getInstance() {
    if (instance == null) {
      var config = new jakarta.json.bind.JsonbConfig().withAdapters(new LocalTimeCustomAdapter(),
          new DateCustomAdapter());
      instance = JsonbBuilder.create(config);
    }
    return instance;
  }

  @Override
  public Jsonb getContext(Class<?> type) {
    return getInstance();
  }

}
