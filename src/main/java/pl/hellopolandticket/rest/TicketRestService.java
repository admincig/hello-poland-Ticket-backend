package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.TicketService;

@Path("/tickets")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TicketRestService {

  @Inject
  private TicketService ticketService;

  @PUT
  @Path("sights/{sightId}/serial-numbers/{serialNumber}")
  public Response punchTicket(
      @PathParam("sightId") Long sightId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.punchTicket(sightId, serialNumber)).build();
  }
}
