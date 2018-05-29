package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_ADMIN;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.PartnerService;
import pl.hellopolandticket.service.dto.PartnerDTO;

@Path("/partners")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class PartnerRestService {

  @Inject
  private PartnerService partnerService;

  @POST
  @RolesAllowed({ROLE_ADMIN})
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }
}
