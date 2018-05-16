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
        .name("Jan Kowalski")
        .password(passwordHash.encode("hellopoland"))
        .email("hellopoland@hellopoland.pl")
        .authorities(Collections.unmodifiableSet(new HashSet<>(asList(ROLE_USER))))
        .build();

    User admin = User.builder()
        .name("Andrzej Nowak")
        .password(passwordHash.encode("zoo"))
        .email("zoo@zoo.pl")
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
