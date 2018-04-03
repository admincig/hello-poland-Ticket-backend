package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.dto.JsonCollectionWrapper;

@Path("/sights")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SightRestService {

  @Inject
  private SightService sightService;

  @GET
  public Response getSights() {
    JsonCollectionWrapper responseBody = JsonCollectionWrapper.builder()
        .items(sightService.findAll())
        .build();

    return Response.ok(responseBody).build();
  }

  @GET
  @Path("/{sightId}")
  public Response getById(@PathParam("sightId") Long sightId) {
    return Response.ok(sightService.findById(sightId)).build();
  }
}
