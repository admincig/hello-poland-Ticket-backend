package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.TicketService;

@Path("/ticket-pools")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketPoolRestService extends RestServiceSuperclass {

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @Inject
  private TicketService ticketService;

  @PATCH
  @Path("/{ticketPoolId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response punchTicket(@PathParam("ticketPoolId") Long ticketPoolId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.punchTicket(currentUser, ticketPoolId, serialNumber)).build();
  }

  @GET
  @Path("/{ticketPoolId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response getTicket(@PathParam("ticketPoolId") Long ticketPoolId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.findBySerialNumber(currentUser, ticketPoolId, serialNumber))
        .build();
  }
}
