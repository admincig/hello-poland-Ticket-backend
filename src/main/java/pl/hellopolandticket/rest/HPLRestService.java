package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.HPLService;

@Path("/hpl")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class HPLRestService {

  @Inject
  private HPLService hplService;

  @GET
  @Path("/push")
  public Response pushDataToHPL() {
    hplService.pushDataToHPL();

    return Response.ok().build();
  }
}
