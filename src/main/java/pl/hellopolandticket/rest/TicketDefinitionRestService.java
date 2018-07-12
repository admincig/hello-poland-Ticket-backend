package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.TicketDefinitionDTO;
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
  private CurrentUser currentUser;

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response add(TicketDefinitionDTO ticketDefinitionDTO) {
    return Response.ok(ticketDefinitionService.add(ticketDefinitionDTO, currentUser)).build();
  }

}
