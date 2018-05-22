package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;

import java.util.Date;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.validator.TicketValidator;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketValidator ticketValidator;

  @Inject
  private UserService userService;

  public TicketDTO punchTicket(CurrentUser currentUser, Long sightEventId,
      String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    ticketValidator.validateAccessingProperTicket(sightEventId, ticket.getSightEvent().getId());
    ticketValidator.validateTicketHasDemandedStatus(ticket);
    ticketValidator.validateProperTime(ticket);

    User ticketTaker = userService.findUserByEmail(currentUser.getEmail());

    ticketValidator.validateTicketTakerHasAccessToSight(ticket.getSightEvent().getSight(),
        ticketTaker.getPartner().getSights());

    ticket.setTicketTaker(ticketTaker);
    ticket.setStatus(PUNCHED);
    ticket.setPunchingDate(new Date());

    return ofTicket(ticket);
  }

  public TicketDTO findBySerialNumber(CurrentUser currentUser, Long sightEventId,
      String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);
    User ticketTaker = userService.findUserByEmail(currentUser.getEmail());

    ticketValidator.validateTicketTakerHasAccessToSight(ticket.getSightEvent().getSight(),
        ticketTaker.getPartner().getSights());
    ticketValidator.validateAccessingProperTicket(sightEventId, ticket.getSightEvent().getId());

    return ofTicket(ticket);
  }


}