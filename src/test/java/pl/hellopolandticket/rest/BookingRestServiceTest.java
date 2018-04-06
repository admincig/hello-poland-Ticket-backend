package pl.hellopolandticket.rest;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static pl.hellopolandticket.model.Status.BOOKED;
import static pl.hellopolandticket.model.Status.BOUGHT;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.service.dto.BookingDTO;
import pl.hellopolandticket.service.dto.BookingDTOCreate;
import pl.hellopolandticket.service.dto.TicketBookingDTO;
import pl.hellopolandticket.service.exception.NoAvailableTicketsException;
import pl.hellopolandticket.service.exception.NotBookedException;
import pl.hellopolandticket.service.exception.NumberOfTicketsNotPositiveException;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Slf4j
@UsingDataSet("scripts/datasets/import.yml")
public class BookingRestServiceTest extends BaseTest {

  @Inject
  private BookingRestService bookingRestService;

  @Inject
  private BookingDao bookingDao;

  @Test
  public void shouldBookTickets() {
    BookingDTOCreate bookingCreate = createBookingDTOCreate();

    Response response = bookingRestService.bookTickets(bookingCreate);
    BookingDTO booking = (BookingDTO) response.getEntity();

    int expectedNumberOfTickets = bookingCreate.getTicketBookings().stream()
        .mapToInt(t -> t.getNumberOfTickets().intValue())
        .sum();

    assertEquals(bookingCreate.getCustomerName(), booking.getCustomerName());
    assertEquals(bookingCreate.getCustomerEmail(), booking.getCustomerEmail());
    assertEquals(expectedNumberOfTickets, booking.getTickets().size());
  }

  @Test
  public void shouldBuyTickets() {
    Booking bookingBeforeBoughtRequest = bookingDao.findById(1L);
    assertEquals(BOOKED, bookingBeforeBoughtRequest.getStatus());
    bookingBeforeBoughtRequest.getTickets()
        .forEach(ticket -> assertEquals(BOOKED, ticket.getStatus()));

    Response response = bookingRestService.buyTickets(1L);
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(BOUGHT, booking.getStatus());
    booking.getTickets().forEach(ticket -> assertEquals(BOUGHT, ticket.getStatus()));
  }

  @Test
  public void shouldBuyTicketsWhenPreviousBookingIsInvalidAndExistsAvailableTickets() {
    Booking b = bookingDao.findById(1L);
    b.makeInvalid();

    Response response = bookingRestService.buyTickets(1L);
    BookingDTO booking = (BookingDTO) response.getEntity();

    assertEquals(booking.getStatus(), BOUGHT);
    booking.getTickets().forEach(ticket -> assertEquals(BOUGHT, ticket.getStatus()));
    assertEquals(b.getTickets().size(), booking.getTickets().size());
    assertEquals(b.getCustomerName(), booking.getCustomerName());
    assertEquals(b.getCustomerEmail(), booking.getCustomerEmail());
  }

  @Test(expected = NoAvailableTicketsException.class)
  public void shouldThrowNoAvailableTicketsTryingToBuyWhenBookingIsInvalidAndSomeoneBookedTickets() {
    Booking b = bookingDao.findById(1L);
    b.makeInvalid();

    BookingDTOCreate bookingCreate = createBookingDTOCreate();
    bookingRestService.bookTickets(bookingCreate);
    bookingRestService.bookTickets(bookingCreate);
    bookingRestService.bookTickets(bookingCreate);

    bookingRestService.buyTickets(1L);
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsBought() {
    bookingRestService.buyTickets(2L);
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsPunched() {
    bookingRestService.buyTickets(3L);
  }

  @Test(expected = NotBookedException.class)
  public void shouldThrowNotBookedExceptionWhenBookingStatusIsDeleted() {
    bookingRestService.buyTickets(4L);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundException() {
    bookingRestService.buyTickets(200L);
  }

  @Test(expected = NumberOfTicketsNotPositiveException.class)
  public void shouldThrowNumberOfTicketsNotPositiveExceptionWhenBookingZeroNumberOfTickets() {
    bookingRestService.bookTickets(createBookingDTOCreateWithZeroTickets());
  }

  @Test(expected = NoAvailableTicketsException.class)
  public void shouldThrowNoAvailableTicketsExceptionTryingToBookTooManyTickets() {
    bookingRestService.bookTickets(createBookingDTOCreate());
    bookingRestService.bookTickets(createBookingDTOCreate());
    bookingRestService.bookTickets(createBookingDTOCreate());
  }

  private BookingDTOCreate createBookingDTOCreate() {
    return BookingDTOCreate.builder()
        .customerName("Jan Kowalski")
        .customerEmail("jan.kowalski@mail.com")
        .ticketBookings(singletonList(TicketBookingDTO.builder()
            .ticketDefinitionId(1L)
            .numberOfTickets(2L)
            .build()))
        .build();
  }

  private BookingDTOCreate createBookingDTOCreateWithZeroTickets() {
    return BookingDTOCreate.builder()
        .customerName("Jan Kowalski")
        .customerEmail("jan.kowalski@mail.com")
        .ticketBookings(singletonList(TicketBookingDTO.builder()
            .ticketDefinitionId(1L)
            .numberOfTickets(0L)
            .build()))
        .build();
  }
}