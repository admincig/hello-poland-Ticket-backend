package pl.hellopolandticket.app;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.security.password.PasswordEncoder;


@Slf4j
@Startup
@Singleton
// @Interceptors(value = LoggingHandler.class)
public class Configuration {

  @Produces
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder();
  }
}
