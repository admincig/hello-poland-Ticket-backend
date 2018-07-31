package pl.hellopolandticket.security;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;


@RequestScoped
public class AuthenticatedUserInfoProducer {

  private CurrentUser currentUser;

  @Produces
  @Authenticated
  public CurrentUser getUserInfo() {
    return this.currentUser;
  }

  public void handleAuthenticationEvent(@Observes @Authenticated CurrentUser authenticatedUser) {
    this.currentUser = authenticatedUser;
  }

}
