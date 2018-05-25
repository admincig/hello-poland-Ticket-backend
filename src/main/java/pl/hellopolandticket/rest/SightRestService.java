package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.Role.ROLE_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.dto.JsonCollectionWrapper;

@Path("/sights")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SightRestService {

  @Inject
  private SightService sightService;

  @Inject
  @Authenticated
  private CurrentUser currentUser;

  @GET
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response getSights() {
    JsonCollectionWrapper responseBody = JsonCollectionWrapper.builder()
        .items(sightService.findForPartner(currentUser.getPrincipal()))
        .build();

    return Response.ok(responseBody).build();
  }

  @GET
  @Path("/{sightId}")
  @RolesAllowed({ROLE_USER})
  public Response getById(@PathParam("sightId") Long sightId) {
    return Response.ok(sightService.findById(sightId)).build();
  }

}
