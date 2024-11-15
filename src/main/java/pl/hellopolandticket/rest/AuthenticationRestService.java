package pl.hellopolandticket.rest;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofCurrentUser;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;

@Path("/")
@RequestScoped
public class AuthenticationRestService extends RestServiceSuperclass {

  @Inject
  private SecurityContext securityContext;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  @Path("login")
  public Response login() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(ofCurrentUser(currentUser)).build();
    }
    return Response.status(UNAUTHORIZED).build();
  }

  @POST
  @Path("refresh")
  public Response refresh() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(ofCurrentUser(currentUser)).build();
    }
    return Response.status(UNAUTHORIZED).build();
  }

  @POST
  @Path("logout")
  public Response logout() {
    return Response.ok().build();
  }

}
