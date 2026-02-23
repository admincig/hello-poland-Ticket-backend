package pl.hellopolandticket.rest;

import java.util.List;

import jakarta.annotation.security.PermitAll;
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
  public Response getList(@QueryParam("atnaIds") List<Long> atnaIds) {
    return Response.ok(ticketDefinitionService.getList(atnaIds, currentUser)).build();
  }

  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") Long id) {
    return Response.ok(ticketDefinitionService.get(id, currentUser)).build();
  }
  @GET
  @Path("/market")
  @PermitAll
  public Response getListForMarket(@QueryParam("atnaIds") List<Long> atnaIds) {
        return Response.ok(ticketDefinitionService.getListForMarket(atnaIds)).build();
  }

}
