package pl.hellopolandticket.app;

import java.io.IOException;
import java.io.InputStream;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.security.password.PasswordEncoder;


@Slf4j
@Startup
@Singleton
public class Configuration {

  public Configuration() {
    java.util.Properties systemProps = System.getProperties();
    try (InputStream customProps = Configuration.class.getResourceAsStream("/config.properties")) {
      systemProps.load(customProps);
    } catch (IOException e) {
      log.warn("Failed to load custom properties. {}", e.getMessage());
    }
  }

  @Produces
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder();
  }
}
