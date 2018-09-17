package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.service.PartnerService;

@Path("/partners")
@RequestScoped
public class PartnerRestService extends RestServiceSuperclass {

  @Inject
  private PartnerService partnerService;

  @POST
  @RolesAllowed({ROLE_ADMIN})
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

}
