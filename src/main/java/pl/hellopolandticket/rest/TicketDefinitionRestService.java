package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.TicketDefinition;
import pl.hellopolandticket.service.TicketDefinitionService;

@Path("/ticket-definitions")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketDefinitionRestService {

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response add(TicketDefinition ticketDefinitionDTO) {
    return Response.ok(ticketDefinitionService.add(ticketDefinitionDTO)).build();
  }

}
