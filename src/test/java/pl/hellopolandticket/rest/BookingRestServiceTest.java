package pl.hellopolandticket.rest;

import static org.junit.Assert.assertEquals;
import static pl.hellopolandticket.model.Status.BOOKED;
import static pl.hellopolandticket.model.Status.BOUGHT;

import java.util.Arrays;
import java.util.Date;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Ignore;
import org.junit.Test;
import pl.hellopoland.dto.booking.Ticket;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.service.dto.BookingDTO;
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
    pl.hellopoland.dto.booking.Booking bookingCreate = createBookingDTOCreate();

    Response response = bookingRestService.makeBooking(bookingCreate);
    BookingDTO booking = (BookingDTO) response.getEntity();

    int expectedNumberOfTickets = Arrays.stream(bookingCreate.ticketBookings)
        .mapToInt(t -> t.numberOfTickets.intValue())
        .sum();

    assertEquals(bookingCreate.customerName, booking.getCustomerName());
    assertEquals(bookingCreate.customerEmail, booking.getCustomerEmail());
    assertEquals(expectedNumberOfTickets, booking.getTickets().size());
  }

  @Test
  public void shouldBuyTickets() {
    Booking bookingBeforeBoughtRequest = bookingDao.findBySerialNumber("1");
    assertEquals(BOOKED, bookingBeforeBoughtRequest.getStatus());
    bookingBeforeBoughtRequest.getTickets()
        .forEach(ticket -> assertEquals(BOOKED, ticket.getStatus()));

    Response response = bookingRestService.markBookingAsBought("1");
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(BOUGHT, booking.getStatus());
    booking.getTickets().forEach(ticket -> assertEquals(BOUGHT, ticket.getStatus()));
  }

  @Test
  public void shouldBuyTicketsWhenPreviousBookingIsInvalidAndExistsAvailableTickets() {
    Booking b = bookingDao.findBySerialNumber("1");
    b.makeInvalid();

    Response response = bookingRestService.markBookingAsBought("1");
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(booking.getStatus(), BOUGHT);
    booking.getTickets().forEach(ticket -> assertEquals(BOUGHT, ticket.getStatus()));
    assertEquals(b.getTickets().size(), booking.getTickets().size());
    assertEquals(b.getCustomerName(), booking.getCustomerName());
    assertEquals(b.getCustomerEmail(), booking.getCustomerEmail());
  }

  @Test(expected = NoAvailableTicketsException.class)
  public void shouldThrowNoAvailableTicketsTryingToBuyWhenBookingIsInvalidAndSomeoneBookedTickets() {
    Booking b = bookingDao.findBySerialNumber("1");
    b.makeInvalid();

    pl.hellopoland.dto.booking.Booking bookingCreate = createBookingDTOCreate();
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

  private pl.hellopoland.dto.booking.Booking createBookingDTOCreate() {
    pl.hellopoland.dto.booking.Booking booking = new pl.hellopoland.dto.booking.Booking();

    Ticket ticket = new Ticket();
    ticket.ticketDefinitionId = 1L;
    ticket.numberOfTickets = 2L;
    ticket.date = new Date();

    booking.customerName = "Jan Kowalski";
    booking.customerEmail = "jan.kowalski@mail.com";
    booking.ticketBookings = new Ticket[]{ticket};

    return booking;
  }

  private pl.hellopoland.dto.booking.Booking createBookingDTOCreateWithZeroTickets() {
    pl.hellopoland.dto.booking.Booking booking = new pl.hellopoland.dto.booking.Booking();

    Ticket ticket = new Ticket();
    ticket.ticketDefinitionId = 1L;
    ticket.numberOfTickets = 0L;
    ticket.date = new Date();

    booking.customerName = "Jan Kowalski";
    booking.customerEmail = "jan.kowalski@mail.com";
    booking.ticketBookings = new Ticket[]{ticket};

    return booking;
  }
}