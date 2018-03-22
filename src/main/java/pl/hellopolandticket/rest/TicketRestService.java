package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
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

  @GET
  @Path("/book/ticket-definitions/{ticketDefinitionId}")
  public Response bookTicket(@PathParam("ticketDefinitionId") Long ticketDefinitionId) {

    ticketService.bookTicketForSight(ticketDefinitionId);

    return Response.ok().build();
  }
}