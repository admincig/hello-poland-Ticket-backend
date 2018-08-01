package pl.hellopolandticket.rest;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonbConfig implements ContextResolver<Jsonb> {

  @Override
  public Jsonb getContext(Class<?> type) {
    javax.json.bind.JsonbConfig config =
        new javax.json.bind.JsonbConfig().withDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", null);
    return JsonbBuilder.create(config);
  }

}
