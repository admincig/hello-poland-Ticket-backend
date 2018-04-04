package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.Ticket.Status.BOOKED;
import static pl.hellopolandticket.model.Ticket.Status.BOUGHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.dto.TicketDefinitionNumberDTO;
import pl.hellopolandticket.service.exception.TicketNotBookedException;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  public synchronized List<TicketDTO> bookTickets(
      List<TicketDefinitionNumberDTO> ticketDefinitionNumberDTOs) {

    List<Ticket> bookedTickets = new ArrayList<>();

    for (TicketDefinitionNumberDTO ticketDefinitionNumberDTO : ticketDefinitionNumberDTOs) {
      TicketDefinition ticketDefinition = ticketDefinitionDao
          .findById(ticketDefinitionNumberDTO.getTicketDefinitionId());

      Sight sight = ticketDefinition.getSight();

      for (int i = 0; i < ticketDefinitionNumberDTO.getNumberOfTickets(); i++) {
        Ticket ticket = Ticket.builder()
            .sight(sight)
            .name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice())
            .date(new Date())
            .status(BOOKED)
            .customerName(ticketDefinitionNumberDTO.getCustomerName())
            .customerEmail(ticketDefinitionNumberDTO.getCustomerEmail())
            .build();

        bookedTickets.add(ticket);
      }

      sight.decreaseAvailableTicketsNumber(
          ticketDefinitionNumberDTO.getNumberOfTickets().intValue());
    }

    ticketDao.persist(bookedTickets);

    return bookedTickets.stream()
        .map(TicketDTO::ofTicket)
        .collect(toList());
  }

  public List<TicketDTO> buyTickets(List<Long> ticketIds) {
    List<Ticket> tickets = ticketDao.findByIdsIn(ticketIds);

    return tickets.stream()
        .map(this::buyTicket)
        .map(TicketDTO::ofTicket)
        .collect(toList());
  }

  private Ticket buyTicket(Ticket ticket) {
    if (ticket.getStatus() == BOOKED) {
      ticket.setStatus(BOUGHT);
      ticket.setSerialNumber(UUID.randomUUID());
    } else {
      throw new TicketNotBookedException();
    }

    return ticket;
  }
}