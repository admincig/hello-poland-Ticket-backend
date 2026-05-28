package pl.hellopolandticket.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.hellopolandticket.service.TicketTypeService;

@Path("/ticket-types")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketTypeRestService {

  @Inject
  private TicketTypeService ticketTypeService;

  @GET
  public Response getList() {
    return Response.ok(ticketTypeService.getActiveList()).build();
  }

}
