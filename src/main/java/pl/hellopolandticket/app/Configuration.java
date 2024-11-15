package pl.hellopolandticket.app;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.security.password.PasswordEncoder;


@Slf4j
@Startup
@Singleton
// @Interceptors(value = LoggingHandler.class)
//@DataSourceDefinition(
//    name = "java:global/jdbc/hellopolandticketDS",
//    className = "org.postgresql.xa.PGXADataSource",
//    serverName = "database",
//    portNumber = 5432,
//    databaseName = "helloticket",
//    user = "helloticket",
//    password = "helloticket")
public class Configuration {

  @Produces
  public PasswordEncoder passwordEncoder() {
    return new PasswordEncoder();
  }
}
