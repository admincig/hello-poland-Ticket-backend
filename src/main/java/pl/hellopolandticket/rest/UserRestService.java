package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.UserService;

@Path("/users")
@RequestScoped
public class UserRestService extends RestServiceSuperclass {

  @Inject
  private UserService userService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @Path("/me")
  @RolesAllowed({ROLE_USER})
  public Response userInfo() {
    return Response.ok(userService.findByEmail(currentUser.getPrincipal())).build();
  }
}
