package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.service.PartnerService;

@Path("/helpdesk")
@RequestScoped
public class HelpdeskRestService extends RestServiceSuperclass {

  @Inject
  private PartnerService partnerService;

  @POST
  @Path("/partners")
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

}
