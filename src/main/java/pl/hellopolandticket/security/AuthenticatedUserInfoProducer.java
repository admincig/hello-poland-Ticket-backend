package pl.hellopolandticket.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;


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
