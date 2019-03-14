package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.HPLService;

@Path("/hpl")
@RequestScoped
public class HPLRestService extends RestServiceSuperclass {

  @Inject
  private HPLService hplService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @Path("/push")
  public Response pushDataToHPL() {
    hplService.pushDataToHPL(currentUser);

    return Response.ok().build();
  }
}
