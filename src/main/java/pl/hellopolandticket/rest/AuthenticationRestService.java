package pl.hellopolandticket.rest;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static pl.hellopolandticket.service.dto.UserAuthDTO.ofCurrentUser;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;

@Path("auth")
@RequestScoped
public class AuthenticationRestService {

  @Inject
  private SecurityContext securityContext;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response login() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(ofCurrentUser(currentUser))
          .build();
    }

    return Response.status(UNAUTHORIZED).build();
  }

  @POST
  @Path("refresh")
  public Response refresh() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(ofCurrentUser(currentUser))
          .build();
    }

    return Response.status(UNAUTHORIZED).build();
  }

  @POST
  @Path("logout")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response logout() {
    return Response.ok().build();
  }

  @GET
  @Path("userinfo")
  public Response userInfo() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(currentUser).build();
    }
    return Response.status(UNAUTHORIZED).build();
  }


}
