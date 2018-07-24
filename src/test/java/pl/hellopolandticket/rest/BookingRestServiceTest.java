package pl.hellopolandticket.rest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static pl.hellopolandticket.model.ticket.market.Status.BOOKED;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Ignore;
import org.junit.Test;
import pl.hellopoland.dto.booking.BookingDTO;
import pl.hellopoland.dto.booking.TicketOrderDTO;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.service.exception.conflict.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.conflict.NotBookedException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;
import pl.hellopolandticket.service.exception.preconditionfailed.NumberOfTicketsNotPositiveException;

@Slf4j
@UsingDataSet("scripts/datasets/import.yml")
@Ignore
public class BookingRestServiceTest extends BaseTest {

  @Inject
  private BookingRestService bookingRestService;

  @Inject
  private BookingDao bookingDao;

  @Test
  public void shouldBookTickets() {
    BookingDTO bookingCreate = createBookingDTOCreate();

    Response response = bookingRestService.makeBooking(bookingCreate);
    BookingDTO booking = (BookingDTO) response.getEntity();

    int expectedNumberOfTickets =
        bookingCreate.ticketBookings.stream().mapToInt(t -> t.numberOfTickets.intValue()).sum();

    assertEquals(bookingCreate.customerName, booking.customerName);
    assertEquals(bookingCreate.customerEmail, booking.customerEmail);
    assertEquals(expectedNumberOfTickets, booking.tickets.size());
  }

  @Test
  public void shouldBuyTickets() {
    Booking bookingBeforeBoughtRequest = bookingDao.findBySerialNumber("1");
    assertEquals(BOOKED, bookingBeforeBoughtRequest.getStatus());
    bookingBeforeBoughtRequest.getTickets()
        .forEach(ticket -> assertEquals(BOOKED, ticket.getStatus()));

    Response response = bookingRestService.markBookingAsBought("1");
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(BOUGHT, Status.valueOf(booking.status.name()));
    booking.tickets.forEach(ticket -> assertEquals(BOUGHT, Status.valueOf(ticket.status.name())));
  }

  @Test
  public void shouldBuyTicketsWhenPreviousBookingIsInvalidAndExistsAvailableTickets() {
    Booking b = bookingDao.findBySerialNumber("1");
    b.makeInvalid();

    Response response = bookingRestService.markBookingAsBought("1");
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(Status.valueOf(booking.status.name()), BOUGHT);
    booking.tickets.forEach(ticket -> assertEquals(BOUGHT, Status.valueOf(ticket.status.name())));
    assertEquals(b.getTickets().size(), booking.tickets.size());
    assertEquals(b.getCustomerName(), booking.customerName);
    assertEquals(b.getCustomerEmail(), booking.customerEmail);
  }

  @Test(expected = NoAvailableTicketsException.class)
  public void shouldThrowNoAvailableTicketsTryingToBuyWhenBookingIsInvalidAndSomeoneBookedTickets() {
    Booking b = bookingDao.findBySerialNumber("1");
    b.makeInvalid();

    BookingDTO bookingCreate = createBookingDTOCreate();
    bookingRestService.makeBooking(bookingCreate);
    bookingRestService.makeBooking(bookingCreate);
    bookingRestService.makeBooking(bookingCreate);

    bookingRestService.markBookingAsBought("1");
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsBought() {
    bookingRestService.markBookingAsBought("2");
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsPunched() {
    bookingRestService.markBookingAsBought("3");
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsDeleted() {
    bookingRestService.markBookingAsBought("4");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundException() {
    bookingRestService.markBookingAsBought("200");
  }

  @Test(expected = NumberOfTicketsNotPositiveException.class)
  public void shouldThrowNumberOfTicketsNotPositiveExceptionWhenBookingZeroNumberOfTickets() {
    bookingRestService.makeBooking(createBookingDTOCreateWithZeroTickets());
  }

  @Test(expected = NoAvailableTicketsException.class)
  public void shouldThrowNoAvailableTicketsExceptionTryingToBookTooManyTickets() {
    bookingRestService.makeBooking(createBookingDTOCreate());
    bookingRestService.makeBooking(createBookingDTOCreate());
    bookingRestService.makeBooking(createBookingDTOCreate());
  }

  private BookingDTO createBookingDTOCreate() {
    BookingDTO booking = new BookingDTO();

    TicketOrderDTO ticket = new TicketOrderDTO();
    ticket.ticketDefinitionId = 1L;
    ticket.numberOfTickets = 2L;

    booking.customerName = "Jan Kowalski";
    booking.customerEmail = "jan.kowalski@mail.com";
    booking.ticketBookings = singletonList(ticket);

    return booking;
  }

  private BookingDTO createBookingDTOCreateWithZeroTickets() {
    BookingDTO booking = new BookingDTO();

    TicketOrderDTO ticket = new TicketOrderDTO();
    ticket.ticketDefinitionId = 1L;
    ticket.numberOfTickets = 0L;

    booking.customerName = "Jan Kowalski";
    booking.customerEmail = "jan.kowalski@mail.com";
    booking.ticketBookings = singletonList(ticket);

    return booking;
  }
}
