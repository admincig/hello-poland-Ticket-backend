package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.TicketPoolDefinitionService;

@Path("/ticket-pool-definitions")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketPoolDefinitionRestService {

  @Inject
  private TicketPoolDefinitionService ticketPoolDefinitionService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @POST
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response add(TicketPoolDefinitionDTO ticketPoolDefinitionDTO) {
    return Response.ok(ticketPoolDefinitionService.add(ticketPoolDefinitionDTO, currentUser))
        .build();
  }

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response getTicketPoolDef() {
    return Response.ok(ticketPoolDefinitionService.getAllForPartner(currentUser)).build();
  }

}
