package pl.hellopolandticket.rest;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofCurrentUser;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
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
