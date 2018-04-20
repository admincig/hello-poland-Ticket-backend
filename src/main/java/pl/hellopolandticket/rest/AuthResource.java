package pl.hellopolandticket.rest;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.UserInfo;

@Path("auth")
@RequestScoped
public class AuthResource {

  @Inject
  private SecurityContext securityContext;

  @Inject
  @Authenticated
  private UserInfo userInfo;

  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response login() {
    if (securityContext.getCallerPrincipal() != null) {
      JsonObject result = Json.createObjectBuilder()
          .add("user", securityContext.getCallerPrincipal().getName())
          .build();
      return Response.ok(result).build();
    }
    return Response.status(UNAUTHORIZED).build();
  }

  @GET
  @Path("userinfo")
  public Response userInfo() {
    if (securityContext.getCallerPrincipal() != null) {
      return Response.ok(userInfo).build();
    }
    return Response.status(UNAUTHORIZED).build();
  }

}
