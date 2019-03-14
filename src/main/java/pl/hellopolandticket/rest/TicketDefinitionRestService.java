package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.TicketDefinitionService;

@Path("/ticket-definitions")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketDefinitionRestService {

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  public Response add(TicketDefinitionDTO ticketDefinitionDTO) {
    return Response.ok(ticketDefinitionService.add(ticketDefinitionDTO, currentUser)).build();
  }

  @GET
  public Response getList() {
    return Response.ok(ticketDefinitionService.getList(currentUser)).build();
  }

}
