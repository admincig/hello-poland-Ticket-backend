package pl.hellopolandticket.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.BookingService;
import pl.hellopolandticket.service.UserService;

@Path("/partners")
@RequestScoped
public class PartnerRestService extends RestServiceSuperclass {

  @Inject
  private UserService userService;
  @Inject
  private BookingService bookingService;
  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  @Path("/ushers")
  public Response createUsher(UserDTO usher) {
    return Response.ok(userService.createUsher(usher)).build();
  }

  @GET
  @Path("/ushers")
  public Response getUshers() {
    return Response.ok(userService.getUshersForCurrnetPartner(currentUser)).build();
  }

  @GET
  @Path("/ushers/{id}")
  public Response getUsher(@PathParam("id") long userId) {
    return Response.ok(userService.getUserByRoleForCurrentPartner(userId, Role.ROLE_USHER)).build();
  }

  @PUT
  @Path("/ushers/{id}")
  public Response updateUsher(@PathParam("id") long userId, UserDTO usher) {
    usher.id = userId;
    return Response.ok(userService.updateUserForCurrnetPartner(usher)).build();
  }

  @GET
  @Path("/bookings/{serialNumber}/sendTicketCopy")
  public Response sendTicketCopy(@PathParam("serialNumber") String serialNumber) {
    return Response.ok(bookingService.sendTicketCopy(serialNumber)).build();
  }

}
