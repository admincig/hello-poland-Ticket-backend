package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.PartnerService;
import pl.hellopolandticket.service.TicketDefinitionService;

@Path("/partners")
@RequestScoped
public class PartnerRestService extends RestServiceSuperclass {

  @Inject
  private PartnerService partnerService;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  private CurrentUser currentUser;

  @POST
  @RolesAllowed({ROLE_ADMIN})
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

  @POST
  @Path("/ticket-definitions")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response add(TicketDefinitionDTO ticketDefinitionDTO) {
    return Response.ok(ticketDefinitionService.add(ticketDefinitionDTO, currentUser)).build();
  }
}
