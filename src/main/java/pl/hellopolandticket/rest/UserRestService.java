package pl.hellopolandticket.rest;

import jakarta.ejb.EJBAccessException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
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
  public Response userInfo() {
    if (currentUser == null) {
      throw new EJBAccessException();
    }
    return Response.ok(userService.findByEmail(currentUser.getPrincipal())).build();
  }

  @PUT
  @Path("/me/password")
  public Response changeMePassword(UserAuthDTO userDTO) {
    userService.changePassword(userDTO, null);
    return Response.ok(new UserAuthDTO()).build();
  }

  @PUT
  @Path("/{id}/password")
  public Response changeUserPassword(@PathParam("id") long usherId, UserAuthDTO userAuthDTO) {
    userService.changePassword(userAuthDTO, usherId);
    return Response.ok(new UserAuthDTO()).build();
  }

}
