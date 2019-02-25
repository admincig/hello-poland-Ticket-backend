package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.UserService;

@Path("/partners")
@RequestScoped
public class PartnerRestService extends RestServiceSuperclass {

  @Inject
  private UserService userService;
  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @Path("/ushers")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response getUshers() {
    return Response.ok(userService.getUshersForCurrnetPartner(currentUser)).build();
  }

  @GET
  @Path("/ushers/{id}")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response getUsher(@PathParam("id") long userId) {
    return Response.ok(userService.getUserForCurrnetPartner(userId)).build();
  }

}
