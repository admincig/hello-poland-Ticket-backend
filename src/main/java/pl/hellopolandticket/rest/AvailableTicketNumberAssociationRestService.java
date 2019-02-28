package pl.hellopolandticket.rest;

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
