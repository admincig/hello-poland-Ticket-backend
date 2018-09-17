package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofCollection;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.CollectionWrapperDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.SightEventService;
import pl.hellopolandticket.service.TicketService;

@Path("/sight-events")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SightEventRestService extends RestServiceSuperclass {

  @Inject
  private SightEventService sightEventService;
  @Inject
  private TicketService ticketService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response getSightEvents(@QueryParam("sightEventIds") List<Long> sightEventIds) {
    CollectionWrapperDTO collectionWrapper;

    if (!sightEventIds.isEmpty()) {
      collectionWrapper = ofCollection(sightEventService.findByIdsIn(sightEventIds));
    } else {
      collectionWrapper =
          ofCollection(sightEventService.findForPartner(currentUser.getPrincipal()));
    }

    return Response.ok(collectionWrapper).build();
  }

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response addSightEvent(SightEventDTO sightEvent) {
    return Response.ok(sightEventService.addSightEvent(sightEvent, currentUser)).build();
  }

  @PUT
  @Path("/{id}")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response updateSightEvent(@PathParam("id") Long id, SightEventDTO sightEvent) {
    return Response.ok(sightEventService.updateSightEvent(id, sightEvent)).build();
  }

  @GET
  @Path("/{sightEventId}")
  @RolesAllowed({ROLE_USER})
  public Response getById(@PathParam("sightEventId") Long sightEventId) {
    return Response.ok(sightEventService.findById(sightEventId)).build();
  }

  @DELETE
  @Path("/{sightEventId}")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response delete(@PathParam("sightEventId") Long sightEventId) {
    sightEventService.delete(sightEventId);

    return Response.noContent().build();
  }

  @PATCH
  @Path("/{sightEventId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response punchTicket(@PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.punchTicket(currentUser, sightEventId, serialNumber)).build();
  }

  @GET
  @Path("/{sightEventId}/tickets/{serialNumber}")
  @RolesAllowed({ROLE_USER})
  public Response getTicket(@PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {

    return Response.ok(ticketService.findBySerialNumber(currentUser, sightEventId, serialNumber))
        .build();
  }

}
