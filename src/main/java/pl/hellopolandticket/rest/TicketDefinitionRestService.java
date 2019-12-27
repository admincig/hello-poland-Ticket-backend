package pl.hellopolandticket.rest;

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

  @PUT
  @Path("/{id}")
  public Response update(@PathParam("id") Long id, TicketDefinitionDTO ticketDefinitionDTO) {
    ticketDefinitionDTO.id = id;
    return Response.ok(ticketDefinitionService.update(ticketDefinitionDTO, currentUser)).build();
  }

  @DELETE
  @Path("/{id}")
  public Response delete(@PathParam("id") Long id) {
    ticketDefinitionService.delete(id, currentUser);
    return Response.ok().build();
  }

  @GET
  public Response getList() {
    return Response.ok(ticketDefinitionService.getList(currentUser)).build();
  }

  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") Long id) {
    return Response.ok(ticketDefinitionService.get(id, currentUser)).build();
  }

}
