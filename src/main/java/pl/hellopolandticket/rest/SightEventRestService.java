package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.Role.ROLE_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.SightEventDefinition;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.SightEventService;
import pl.hellopolandticket.service.TicketService;
import pl.hellopolandticket.service.dto.JsonCollectionWrapper;

@Path("/sight-events")
@RequestScoped
public class SightEventRestService extends RestServiceSuperclass {

  @Inject
  private SightEventService sightEventService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @Inject
  private TicketService ticketService;

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response getSightEvents() {
    JsonCollectionWrapper responseBody = JsonCollectionWrapper.builder()
        .items(sightEventService.findForPartner(currentUser.getPrincipal()))
        .build();

    return Response.ok(responseBody).build();
  }

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response addSightEvent(SightEventDefinition sightEvent) {
    return Response.ok(sightEventService.addSightEvent(sightEvent, currentUser)).build();
  }

  @GET
  @Path("/{sightEventId}")
  @RolesAllowed({ROLE_USER})
  public Response getById(@PathParam("sightEventId") Long sightEventId) {
    return Response.ok(sightEventService.findById(sightEventId)).build();
  }

  @PATCH
  @Path("/{sightEventId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response punchTicket(
      @PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.punchTicket(currentUser, sightEventId, serialNumber))
        .build();
  }

  @GET
  @Path("/{sightEventId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response getTicket(
      @PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.findBySerialNumber(currentUser, sightEventId, serialNumber))
        .build();
  }

}
