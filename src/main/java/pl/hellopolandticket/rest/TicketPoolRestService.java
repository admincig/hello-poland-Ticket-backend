package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USER;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.TicketPoolService;

@Path("/ticket-pools")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TicketPoolRestService {

  @Inject
  private TicketPoolService service;

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response getTicketPoolForSightEvent(@QueryParam("sightEventId") Long id) {
    return Response.ok(service.getForSightEvent(id)).build();
  }

}
