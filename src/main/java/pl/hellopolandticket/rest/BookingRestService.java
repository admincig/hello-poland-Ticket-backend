package pl.hellopolandticket.rest;

import static java.util.Arrays.asList;
import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;

import java.util.Date;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.booking.Booking;
import pl.hellopoland.dto.booking.Ticket;
import pl.hellopolandticket.service.BookingService;
import pl.hellopolandticket.service.dto.BookingDTO;

@Path("/bookings")
@RequestScoped
public class BookingRestService extends RestServiceSuperclass {

  @Inject
  private BookingService bookingService;

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response makeBooking(Booking booking) {
    return Response.ok(bookingService.createBooking(booking)).build();
  }

  @PUT
  @Path("/buy/{serialNumber}")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response markBookingAsBought(@PathParam("serialNumber") String serialNumber) {
    return Response.ok(bookingService.markBookingAsBought(serialNumber)).build();
  }

  @GET
  @Path("/book-buy/{email}")
  public Response bookAndBuy(@PathParam("email") String email) {
    Ticket ticket1 = new Ticket();
    ticket1.ticketDefinitionId = 1L;
    ticket1.numberOfTickets = 2L;
    ticket1.date = new Date();

    Ticket ticket2 = new Ticket();
    ticket2.ticketDefinitionId = 2L;
    ticket2.numberOfTickets = 3L;
    ticket2.date = new Date();

    Ticket ticket3 = new Ticket();
    ticket3.ticketDefinitionId = 3L;
    ticket3.numberOfTickets = 2L;
    ticket3.date = new Date();

    Booking booking = new Booking();
    booking.customerName = "Jan Kowalski";
    booking.customerEmail = email;
    booking.ticketBookings = asList(ticket1, ticket2, ticket3);

    BookingDTO persistedBooking = bookingService.createBooking(booking);

    return Response.ok(bookingService.markBookingAsBought(persistedBooking.getSerialNumber()))
        .build();
  }
}
