package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.service.BookingService;
import pl.hellopolandticket.service.PartnerService;

@Path("/helpdesk")
@RequestScoped
public class HelpdeskRestService extends RestServiceSuperclass {

  @Inject
  private PartnerService partnerService;
  @Inject
  private BookingService bookingService;

  @POST
  @Path("/partners")
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

  @GET
  @Path("/bookings/{serialNumber}/sendTicketCopy")
  public Response sendTicketCopy(@PathParam("serialNumber") String serialNumber) {
    return Response.ok(bookingService.sendTicketCopy(serialNumber)).build();
  }

}
