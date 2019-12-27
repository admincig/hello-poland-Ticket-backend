package pl.hellopolandticket.rest;

import java.time.LocalDate;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
  public Response getTicketPoolDefs() {
    return Response.ok(ticketPoolDefinitionService.getAllForPartner(currentUser)).build();
  }

  @PUT
  @Path("/{id}")
  public Response updateTicketPoolDef(@PathParam("id") Long id, TicketPoolDefinitionDTO dto) {
    dto.id = id;
    ticketPoolDefinitionService.update(dto, currentUser);
    return getTicketPoolDefs();
  }

  @GET
  @Path("/{id}")
  public Response getTicketPoolDef(@PathParam("id") Long id) {
    return Response.ok(ticketPoolDefinitionService.getForPartner(id, currentUser)).build();
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
