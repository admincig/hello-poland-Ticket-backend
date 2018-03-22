package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.TicketStatus.BOOKED;

import java.util.Date;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;

  @Inject
  private SightService sightService;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  public Ticket bookTicketForSight(Long ticketDefinitionId) {
    TicketDefinition ticketDefinition = ticketDefinitionService.findById(ticketDefinitionId);

    Sight sight = ticketDefinition.getSight();

    Ticket ticket = Ticket.builder()
        .sight(sight)
        .name(ticketDefinition.getName())
        .price(ticketDefinition.getPrice())
        .date(new Date())
        .ticketStatus(BOOKED)
        .build();

    sight.decreaseAvailableTicketsNumber();
    sightService.save(sight);

    return ticketDao.persist(ticket);
  }
}