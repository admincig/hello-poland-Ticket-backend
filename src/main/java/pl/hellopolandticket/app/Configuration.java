package pl.hellopolandticket.app;

import java.io.IOException;
import java.io.InputStream;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Startup
public class Configuration {

  public Configuration() {
    java.util.Properties systemProps = System.getProperties();
    try (InputStream customProps = Configuration.class.getResourceAsStream("/config.properties")) {
      systemProps.load(customProps);
    } catch (IOException e) {
      log.warn("Failed to load custom properties. {}", e.getMessage());
    }
  }
}