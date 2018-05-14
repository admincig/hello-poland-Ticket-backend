package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;

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

  public TicketDTO punchTicket(CurrentUser currentUser, Long sightId,
      String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    ticketValidator.validateAccessingProperTicket(sightId, ticket.getSight().getId());
    ticketValidator.validateTicketHasDemandedStatus(ticket);
    ticketValidator.validateProperTime(ticket);

    User ticketTaker = userService.findByEmail(currentUser.getEmail());

    ticket.setTicketTaker(ticketTaker);
    ticket.setStatus(PUNCHED);

    return ofTicket(ticket);
  }

  public TicketDTO findBySerialNumber(Long sightId, String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    ticketValidator.validateAccessingProperTicket(sightId, ticket.getSight().getId());

    return ofTicket(ticket);
  }


}