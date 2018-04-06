package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Status.BOOKED;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Booking;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.dto.TicketBookingDTO;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  public synchronized List<Ticket> bookTickets(List<TicketBookingDTO> ticketBookingDTOS,
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
}