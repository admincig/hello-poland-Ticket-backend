package pl.hellopolandticket.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;

@Singleton
@Startup
public class Configuration {

  public Configuration() {
    java.util.Properties systemProps = System.getProperties();
    try (InputStream customProps = Configuration.class.getResourceAsStream("/config.properties")) {
      systemProps.load(customProps);
    } catch (IOException e) {
    }
  }

  @Produces
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

}