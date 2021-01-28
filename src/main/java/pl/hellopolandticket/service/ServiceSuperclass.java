package pl.hellopolandticket.service;

import java.io.*;
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
      properties = System.getProperties();
    try {
      try (InputStream is = ServiceSuperclass.class.getResourceAsStream("/runtime.properties")) {
        Reader reader = new InputStreamReader(is, "UTF-8");
        properties.load(reader);
      }
      if (properties.containsKey("local.runtime.properties")) {
        try (FileInputStream fis = new FileInputStream(new File((String) properties.get("local.runtime.properties")))) {
          Reader reader = new InputStreamReader(fis, "UTF-8");
          properties.load(reader);
        }
      }
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
