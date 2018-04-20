package pl.hellopolandticket.app;

import static java.util.Arrays.asList;
import static pl.hellopolandticket.model.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.Role.ROLE_USER;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.security.password.PasswordEncoder;


@Slf4j
@Startup
@Singleton
public class Configuration {

  @Inject
  private UserDao userDao;

  public Configuration() {
    java.util.Properties systemProps = System.getProperties();
    try (InputStream customProps = Configuration.class.getResourceAsStream("/config.properties")) {
      systemProps.load(customProps);
    } catch (IOException e) {
      log.warn("Failed to load custom properties. {}", e.getMessage());
    }
  }

  @PostConstruct
  public void init() {
    PasswordEncoder passwordHash = new PasswordEncoder();

    User user = User.builder()
        .username("user")
        .password(passwordHash.encode("password"))
        .email("user@example.com")
        .authorities(Collections.unmodifiableSet(new HashSet<>(asList(ROLE_USER))))
        .build();

    User admin = User.builder()
        .username("admin")
        .password(passwordHash.encode("password"))
        .email("admin@example.com")
        .authorities(Collections.unmodifiableSet(new HashSet<>(asList(ROLE_USER, ROLE_ADMIN))))
        .build();

    userDao.persist(user);
    userDao.persist(admin);
  }

  @Produces
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder();
  }
}
