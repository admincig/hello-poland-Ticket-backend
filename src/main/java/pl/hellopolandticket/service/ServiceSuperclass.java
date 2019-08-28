package pl.hellopolandticket.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.enterprise.SecurityContext;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.model.auth.User;

@Interceptors(value = LoggingHandler.class)
public class ServiceSuperclass {
  protected static Properties properties;
  private static Logger staticLogger = System.getLogger(ServiceSuperclass.class.getName());
  protected Logger logger = System.getLogger(this.getClass().getName());
  
  static {
    try {
      properties = System.getProperties();
      var copy = new HashMap<>(properties);
      properties.clear();
      properties.load(ServiceSuperclass.class.getResourceAsStream("/runtime.properties"));
      if (copy.containsKey("local.runtime.properties")) {
        properties
            .load(new FileInputStream(new File((String) copy.get("local.runtime.properties"))));
      }
      properties.putAll(copy);
      staticLogger.log(Logger.Level.DEBUG,
          properties.entrySet().stream().map(Object::toString).collect(Collectors.joining("\n")));
    } catch (IOException e) {
      staticLogger.log(Logger.Level.WARNING, "Failed to load properties", e);
    }
  }

  @Inject
  protected SecurityContext ctx;
  @PersistenceContext
  protected EntityManager em;

  public User getLoggedUser() {
    try {
      String login = ctx.getCallerPrincipal().getName();
      return em.createQuery("from User where lower(email) = :email", User.class)
          .setParameter("email", login.toLowerCase()).getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

}
