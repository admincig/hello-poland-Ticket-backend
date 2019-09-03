package pl.hellopolandticket.rest;

import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofCollection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBAccessException;
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
import pl.hellopoland.dto.FileDescriptorDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopolandticket.annotation.DateFormat;
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
  public Response getSightEvents(@QueryParam("sightEventIds") List<Long> sightEventIds) {
    CollectionWrapperDTO collectionWrapper;
    if (sightEventIds.isEmpty()) {
      collectionWrapper =
          ofCollection(sightEventService.findForPartner(currentUser.getPrincipal()));
    } else {
      collectionWrapper = ofCollection(sightEventService.findByIdsIn(sightEventIds));
    }
    return Response.ok(collectionWrapper).build();
  }

  @POST
  @Path("/available")
  public Response getAvailableSightEvents(Set<Long> sightEventIds) {
    return Response.ok(sightEventService.getAvailableSightEventsIds(sightEventIds)).build();
  }

  @POST
  @Path("/in-date-range")
  public Response getSightEventsInDateRange(Set<Long> sightEventIds,
      @QueryParam("fromDate") @DateFormat Date fromDate,
      @QueryParam("toDate") @DateFormat Date toDate) {
    return Response
        .ok(sightEventService.getSightEventsIdsInDateRange(sightEventIds, fromDate, toDate))
        .build();
  }

  @POST
  public Response addSightEvent(SightEventDTO sightEvent) {
    return Response.ok(sightEventService.addSightEvent(sightEvent, currentUser)).build();
  }

  @PUT
  @Path("/{id}")
  public Response updateSightEvent(@PathParam("id") Long id, SightEventDTO sightEvent) {
    return Response.ok(sightEventService.updateSightEvent(id, sightEvent)).build();
  }

  @GET
  @Path("/{sightEventId}")
  public Response getById(@PathParam("sightEventId") Long sightEventId) {
    return Response.ok(sightEventService.findById(sightEventId)).build();
  }

  @DELETE
  @Path("/{sightEventId}")
  public Response delete(@PathParam("sightEventId") Long sightEventId) {
    sightEventService.delete(sightEventId);
    return Response.noContent().build();
  }

  @PATCH
  @Path("/{sightEventId}/tickets/{serialNumber}")
  public Response punchTicket(@PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {
    return Response.ok(ticketService.punchTicket(currentUser, sightEventId, serialNumber)).build();
  }

  @GET
  @Path("/{sightEventId}/tickets/{serialNumber}")
  public Response getTicket(@PathParam("sightEventId") Long sightEventId,
      @PathParam("serialNumber") String serialNumber) {
    return Response.ok(ticketService.findBySerialNumber(currentUser, sightEventId, serialNumber))
        .build();
  }

  @DELETE
  @Path("/{id}/sale")
  public Response stopSale(@PathParam("id") Long id, @QueryParam("tpdId") Long tpdId,
      @QueryParam("date") @DateFormat Date date) {
    try {
      sightEventService.stopSale(id, tpdId, date);
      return Response.noContent().build();
    } catch (Exception e) {
      if (!(e instanceof EJBAccessException)) {
        return Response.notModified().build();
      }
      throw e;
    }
  }

  @PUT
  @Path("/{id}/pdf")
  public Response uploadPdf(@PathParam("id") Long id, FileDescriptorDTO pdf) {
    return Response.ok(sightEventService.uploadPdf(id, pdf)).build();
  }

  @DELETE
  @Path("/{id}/pdf/{name}")
  public Response deletePdf(@PathParam("id") Long id, @PathParam("name") String name) {
    sightEventService.removePdf(id, name);
    return Response.ok().build();
  }

}
