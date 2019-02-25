package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.UserAuthDTO;
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

  @PUT
  @Path("/me/password")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response changeMePassword(UserAuthDTO userDTO) {
    userService.changePassword(userDTO, null);
    return Response.ok(new UserAuthDTO()).build();
  }

  @PUT
  @Path("/{id}/password")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response changeUserPassword(@PathParam("id") long usherId, UserAuthDTO userAuthDTO) {
    userService.changePassword(userAuthDTO, usherId);
    return Response.ok(new UserAuthDTO()).build();
  }

  @GET
  @Path("/ushers")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response getUshers() {
    return Response.ok(userService.getUshers(currentUser)).build();
  }

}
