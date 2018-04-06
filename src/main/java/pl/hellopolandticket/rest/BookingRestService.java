package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.BookingService;
import pl.hellopolandticket.service.dto.BookingDTOCreate;

@Path("/bookings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {

  @Inject
  private BookingService bookingService;

  @POST
  public Response bookTickets(BookingDTOCreate booking) {
    return Response.ok(bookingService.bookTickets(booking)).build();
  }

  @PUT
  @Path("/buy/{bookingId}")
  public Response buyTickets(@PathParam("bookingId") Long bookingId) {
    return Response.ok(bookingService.buyTickets(bookingId)).build();
  }
}
