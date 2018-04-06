package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Status.BOOKED;
import static pl.hellopolandticket.model.Status.INVALID;
import static pl.hellopolandticket.service.dto.BookingDTO.ofBooking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.BookingDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.dto.BookingDTO;
import pl.hellopolandticket.service.dto.BookingDTOCreate;
import pl.hellopolandticket.service.dto.TicketBookingDTO;
import pl.hellopolandticket.service.exception.NotBookedException;

@Stateless
@LocalBean
public class BookingService {

  @Inject
  private BookingDao bookingDao;

  @Inject
  private TicketService ticketService;

  public BookingDTO bookTickets(BookingDTOCreate booking) {
    Booking bookingToPersist = Booking.builder()
        .date(new Date())
        .customerName(booking.getCustomerName())
        .customerEmail(booking.getCustomerEmail())
        .build();

    List<Ticket> tickets = ticketService.bookTickets(booking.getTicketBookings(), bookingToPersist);

    bookingToPersist.setTickets(tickets);

    return ofBooking(bookingDao.persist(bookingToPersist));
  }

  public BookingDTO buyTickets(Long bookingId) {
    Booking booking = bookingDao.findById(bookingId);

    if (booking.getStatus() == BOOKED) {
      booking.makeBought();
    } else if (booking.getStatus() == INVALID) {
      BookingDTO renewedBooking = bookTickets(prepareRenewedBookingDTOCreate(booking));

      return buyTickets(renewedBooking.getId());
    } else {
      throw new NotBookedException();
    }

    return ofBooking(booking);
  }

  private BookingDTOCreate prepareRenewedBookingDTOCreate(Booking booking) {
    List<TicketBookingDTO> ticketBookingDTOs = new ArrayList<>();

    for (Ticket ticket : booking.getTickets()) {
      ticketBookingDTOs.add(TicketBookingDTO.builder()
          .ticketDefinitionId(ticket.getTicketDefinition().getId())
          .numberOfTickets(1L)
          .build());
    }

    return BookingDTOCreate.builder()
        .customerName(booking.getCustomerName())
        .customerEmail(booking.getCustomerEmail())
        .ticketBookings(ticketBookingDTOs)
        .build();
  }
}