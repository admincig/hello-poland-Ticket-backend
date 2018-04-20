package pl.hellopolandticket.security;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequestScoped
public class AuthenticatedUserInfoProducer {

  private UserInfo currentUser;

  @Produces
  @Authenticated
  public UserInfo getUserInfo() {
    return this.currentUser;
  }

  public void handleAuthenticationEvent(@Observes @Authenticated UserInfo authenticatedUser) {
    this.currentUser = authenticatedUser;
  }

}
