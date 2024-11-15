package pl.hellopolandticket.rest;

import java.time.LocalDate;
import java.util.List;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.TicketPoolDefinitionService;
import pl.hellopolandticket.service.TicketPoolService;

@Path("/ticket-pool-definitions")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketPoolDefinitionRestService {

  @Inject
  private TicketPoolDefinitionService ticketPoolDefinitionService;
  @Inject
  private TicketPoolService tpService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  public Response add(TicketPoolDefinitionDTO ticketPoolDefinitionDTO) {
    return Response.ok(ticketPoolDefinitionService.add(ticketPoolDefinitionDTO, currentUser))
        .build();
  }

  @GET
  public Response getTicketPoolDefs(@QueryParam("sightEventIds") List<Long> sightEventIds) {
    return Response.ok(ticketPoolDefinitionService.getList(currentUser, sightEventIds))
        .build();
  }

  @PUT
  @Path("/{id}")
  public Response updateTicketPoolDef(@PathParam("id") Long id, TicketPoolDefinitionDTO dto) {
    dto.id = id;
    ticketPoolDefinitionService.update(dto, currentUser);
    return getTicketPoolDef(id);
  }

  @GET
  @Path("/{id}")
  public Response getTicketPoolDef(@PathParam("id") Long id) {
    return Response.ok(ticketPoolDefinitionService.get(id, currentUser)).build();
  }

  @DELETE
  @Path("/{id}")
  public Response deleteTicketPoolDef(@PathParam("id") Long id) {
    ticketPoolDefinitionService.deleteTicketPoolDefinition(id, currentUser);
    return Response.noContent().build();
  }

  @GET
  @Path("/{id}/available-dates")
  public List<LocalDate> availableDates(@PathParam("id") Long id,
      @QueryParam("fromDate") String dateFromStr,
      @QueryParam("toDate") String dateToSt) {
    LocalDate dateFrom = LocalDate.parse(dateFromStr);
    LocalDate dateTo = LocalDate.parse(dateToSt);
    return tpService.getAllStartDatesForInstancesOfCyclicPool(id, dateFrom, dateTo);
  }

  @POST
  @Path("/get-whole-day")
  public Response getWholeDay(List<Long> tpdIds) {
    return Response.ok(ticketPoolDefinitionService.getWholeDay(tpdIds)).build();
  }

}
