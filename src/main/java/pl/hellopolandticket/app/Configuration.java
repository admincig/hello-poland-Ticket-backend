package pl.hellopolandticket.app;

import java.io.IOException;
import java.io.InputStream;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class Configuration {

  private System.Logger logger = System.getLogger(Configuration.class.getName());

  public Configuration() {
    java.util.Properties systemProps = System.getProperties();
    try (InputStream customProps = Configuration.class.getResourceAsStream("/config.properties")) {
      systemProps.load(customProps);
    } catch (IOException e) {
      logger.log(System.Logger.Level.WARNING, "Failed to load custom properties", e);
    }
  }

}
