package pl.hellopolandticket.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Properties;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;

@Interceptors(value = LoggingHandler.class)
public class ServiceSuperclass {
  protected static Properties properties;
  private static Logger staticLogger = System.getLogger(ServiceSuperclass.class.getName());

  static {
    try {
      properties = System.getProperties();
      var copy = new HashMap<>(properties);
      properties.clear();
      properties.load(ServiceSuperclass.class.getResourceAsStream("/etc/config.properties"));
      properties.load(ServiceSuperclass.class
          .getResourceAsStream("/etc/" + copy.get("user.name") + ".config.properties"));
      if (copy.containsKey("local.properties")) {
        properties.load(new FileInputStream(new File((String) copy.get("local.properties"))));
      }
      properties.putAll(copy);
    } catch (IOException e) {
      staticLogger.log(Logger.Level.WARNING, "Failed to load properties", e);
    }
  }

}
