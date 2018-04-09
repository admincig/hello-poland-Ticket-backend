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
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
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
  private TicketDao ticketDao;

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  public BookingDTO createBooking(BookingDTOCreate booking) {
    Booking bookingToPersist = Booking.builder()
        .date(new Date())
        .customerName(booking.getCustomerName())
        .customerEmail(booking.getCustomerEmail())
        .build();

    List<Ticket> tickets = bookTickets(booking.getTicketBookings(), bookingToPersist);

    bookingToPersist.setTickets(tickets);

    return ofBooking(bookingDao.persist(bookingToPersist));
  }

  public BookingDTO markBookingAsBought(Long bookingId) {
    Booking booking = bookingDao.findById(bookingId);

    if (booking.getStatus() == BOOKED) {
      booking.makeBought();
    } else if (booking.getStatus() == INVALID) {
      bookTickets(null, booking);

      return markBookingAsBought(booking.getId());
    } else {
      throw new NotBookedException();
    }

    return ofBooking(booking);
  }

  private synchronized List<Ticket> bookTickets(List<TicketBookingDTO> ticketBookingDTOS,
      Booking booking) {
    if (isANewBooking(ticketBookingDTOS)) {
      return book(ticketBookingDTOS, booking);
    } else {
      return rebook(booking);
    }
  }

  private List<Ticket> book(List<TicketBookingDTO> ticketBookingDTOS,
      Booking booking) {
    List<Ticket> bookedTickets = new ArrayList<>();

    for (TicketBookingDTO ticketBookingDTO : ticketBookingDTOS) {
      TicketDefinition ticketDefinition = ticketDefinitionDao
          .findById(ticketBookingDTO.getTicketDefinitionId());

      Sight sight = ticketDefinition.getSight();

      for (int i = 0; i < ticketBookingDTO.getNumberOfTickets(); i++) {
        Ticket ticket = Ticket.builder()
            .sight(sight)
            .name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice())
            .date(booking.getDate())
            .status(BOOKED)
            .booking(booking)
            .ticketDefinition(ticketDefinition)
            .build();

        bookedTickets.add(ticket);
      }

      sight.decreaseAvailableTicketsNumber(
          ticketBookingDTO.getNumberOfTickets().intValue());
    }

    return ticketDao.persist(bookedTickets);
  }

  private List<Ticket> rebook(Booking booking) {
    List<Ticket> tickets = booking.getTickets();

    for (Ticket ticket : tickets) {
      ticket.getSight().decreaseAvailableTicketsNumber(1);
      ticket.setStatus(BOOKED);
    }
    booking.setStatus(BOOKED);

    return tickets;
  }

  private boolean isANewBooking(List<TicketBookingDTO> ticketBookingDTOs) {
    return ticketBookingDTOs != null;
  }
}