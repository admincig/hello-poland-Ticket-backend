package pl.hellopolandticket.rest;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.TicketService;
import pl.hellopolandticket.service.dto.JsonCollectionWrapper;

@Path("/sights")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SightRestService {

  @Inject
  private SightService sightService;

  @Inject
  private TicketService ticketService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @RolesAllowed({"ROLE_USER"})
  public Response getSights() {
    JsonCollectionWrapper responseBody = JsonCollectionWrapper.builder()
        .items(sightService.findForPartner(currentUser.getEmail()))
        .build();

    return Response.ok(responseBody).build();
  }

  @GET
  @Path("/{sightId}")
  @RolesAllowed({"ROLE_USER"})
  public Response getById(@PathParam("sightId") Long sightId) {
    return Response.ok(sightService.findById(sightId)).build();
  }

  @PATCH
  @Path("/{sightId}/tickets/{serialNumber}")
  @RolesAllowed({"ROLE_USER"})
  public Response punchTicket(
      @PathParam("sightId") Long sightId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.punchTicket(currentUser, sightId, serialNumber)).build();
  }

  @GET
  @Path("/{sightId}/tickets/{serialNumber}")
  @RolesAllowed({"ROLE_USER"})
  public Response getTicket(
      @PathParam("sightId") Long sightId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.findBySerialNumber(currentUser, sightId, serialNumber))
        .build();
  }
}
