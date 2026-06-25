package pl.hellopolandticket.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.rest.dto.TicketEmailRequest;
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
  public Response addPartner(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

  @DELETE
  @Path("/partners/{email}")
  public Response removePartner(@PathParam("email") String partnerEmail) {
    partnerService.removeNewCreatedPartner(partnerEmail);
    return Response.noContent().build();
  }

  @GET
  @Path("/bookings/{serialNumber}/sendTicketCopy")
  public Response sendTicketCopy(@PathParam("serialNumber") String serialNumber) {
    return Response.ok(bookingService.sendTicketCopy(serialNumber)).build();
  }

  @POST
  @Path("/bookings/{serialNumber}/sendTicketCopyToEmail")
  public Response sendTicketCopyToEmail(@PathParam("serialNumber") String serialNumber,
      TicketEmailRequest request) {
    return Response.ok(
        bookingService.sendTicketCopyToEmail(serialNumber, request == null ? null : request.email))
        .build();
  }

}
