package pl.hellopolandticket.rest;

import java.util.Date;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pl.hellopolandticket.annotation.DateFormat;
import pl.hellopolandticket.service.AvailableTicketNumberAssociationService;

@Path("/available-ticket-number-associations")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AvailableTicketNumberAssociationRestService {

  @Inject
  private AvailableTicketNumberAssociationService service;

  @GET
  public Response checkAvailabilityOfTickets(@QueryParam("sightEventId") Long id,
      @QueryParam("fromDate") @DateFormat Date fromDate,
      @QueryParam("toDate") @DateFormat Date toDate) {
    return Response.ok(service.checkAvailabilityOfTickets(id, fromDate, toDate)).build();
  }

}
