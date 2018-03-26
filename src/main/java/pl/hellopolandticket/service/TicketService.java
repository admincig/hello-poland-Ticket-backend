package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.TicketStatus.BOOKED;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.dto.TicketDefinitionNumberDTO;

@Stateless
@LocalBean
public class TicketService {

  @EJB
  private TicketDao ticketDao;

  @EJB
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
            .ticketStatus(BOOKED)
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

}