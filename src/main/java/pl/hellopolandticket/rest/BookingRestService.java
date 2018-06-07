package pl.hellopolandticket.rest;

import static java.util.Arrays.asList;
import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.service.BookingService;
import pl.hellopolandticket.service.dto.BookingDTO;
import pl.hellopolandticket.service.dto.BookingDTOCreate;
import pl.hellopolandticket.service.dto.TicketBookingDTO;

@Path("/bookings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Interceptors(value = LoggingHandler.class)
public class BookingRestService {

  @Inject
  private BookingService bookingService;

  @POST
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response makeBooking(BookingDTOCreate booking) {
    return Response.ok(bookingService.createBooking(booking)).build();
  }

  @PUT
  @Path("/buy/{bookingId}")
  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Response markBookingAsBought(@PathParam("bookingId") Long bookingId) {
    return Response.ok(bookingService.markBookingAsBought(bookingId)).build();
  }

  @GET
  @Path("/book-buy/{email}")
  public Response bookAndBuy(@PathParam("email") String email) {
    TicketBookingDTO ticketBooking1 = TicketBookingDTO.builder()
        .ticketDefinitionId(1L)
        .numberOfTickets(2L)
        .build();
    TicketBookingDTO ticketBooking2 = TicketBookingDTO.builder()
        .ticketDefinitionId(2L)
        .numberOfTickets(3L)
        .build();
    TicketBookingDTO ticketBooking3 = TicketBookingDTO.builder()
        .ticketDefinitionId(3L)
        .numberOfTickets(2L)
        .build();

    BookingDTOCreate booking = BookingDTOCreate.builder()
        .customerName("Jan Kowalski")
        .customerEmail(email)
        .ticketBookings(asList(ticketBooking1, ticketBooking2, ticketBooking3))
        .build();

    BookingDTO persistedBooking = bookingService.createBooking(booking);

    return Response.ok(bookingService.markBookingAsBought(persistedBooking.getId())).build();
  }
}
