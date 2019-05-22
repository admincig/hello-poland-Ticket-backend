package pl.hellopolandticket.rest;

import static java.util.Arrays.asList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.booking.BookingDTO;
import pl.hellopoland.dto.booking.TicketOrderDTO;
import pl.hellopolandticket.service.BookingService;

@Path("/bookings")
@RequestScoped
public class BookingRestService extends RestServiceSuperclass {

  @Inject
  private BookingService bookingService;

  @POST
  public Response makeBooking(BookingDTO booking) {
    return Response.ok(bookingService.createBooking(booking)).build();
  }

  @PUT
  @Path("/buy/{serialNumber}/{p24OrderId}/{p24Currency}")
  public Response markBookingAsBought(@PathParam("serialNumber") String serialNumber,
      @PathParam("p24OrderId") String p24OrderId, @PathParam("p24Currency") String p24Currency) {
    return Response.ok(bookingService.markBookingAsBought(serialNumber, p24OrderId, p24Currency))
        .build();
  }

  @GET
  @Path("/book-buy/{email}")
  public Response bookAndBuy(@PathParam("email") String email) throws ParseException {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = df.parse("2018-08-03 10:00:00");
    TicketOrderDTO ticket1 = new TicketOrderDTO();
    ticket1.ticketDefinitionId = 1L;
    ticket1.ticketPoolDefinitionId = 1L;
    ticket1.numberOfTickets = 2L;
    ticket1.date = date;

    // TicketOrderDTO ticket2 = new TicketOrderDTO();
    // ticket2.ticketDefinitionId = 2L;
    // ticket2.ticketPoolDefinitionId = 2L;
    // ticket2.numberOfTickets = 3L;
    // ticket2.date = date;

    // TicketOrderDTO ticket3 = new TicketOrderDTO();
    // ticket3.ticketDefinitionId = 3L;
    // ticket3.ticketPoolDefinitionId = 3L;
    // ticket3.numberOfTickets = 2L;
    // ticket3.date = date;

    BookingDTO booking = new BookingDTO();
    booking.customerName = "Jan Kowalski";
    booking.customerEmail = email;
    booking.ticketBookings = asList(ticket1);
    // booking.ticketBookings = asList(ticket1, ticket2, ticket3);

    BookingDTO persistedBooking = bookingService.createBooking(booking);

    return Response.ok(bookingService.markBookingAsBought(persistedBooking.serialNumber,
        "P24-from_/book-buy/", "PLN")).build();
  }
}
