package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.SightService;

@Path("/sights")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SightRestService {

  @Inject
  private SightService sightService;

  @GET
  public Response getSights() {
    return Response.ok(sightService.findAll()).build();
  }
}
