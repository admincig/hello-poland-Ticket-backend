package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicket;
import java.util.Date;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.validator.TicketValidator;

@Stateless
@LocalBean
public class TicketService extends ServiceSuperclass {
  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketValidator ticketValidator;

  @Inject
  private UserService userService;

  @RolesAllowed({ROLE_USHER})
  public TicketDTO punchTicket(CurrentUser currentUser, Long sightEventId, String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    ticketValidator.validateAccessingProperTicket(sightEventId,
        ticket.getTicketPool().getTicketPoolDefinition().getSightEvent().getId());

    ticketValidator.validateTicketHasDemandedStatus(ticket);
    ticketValidator.validateProperTime(ticket);

    User ticketTaker = userService.findUserByEmail(currentUser.getPrincipal());

    ticketValidator.validateTicketTakerHasAccessToSightEvent(
        ticket.getTicketPool().getTicketPoolDefinition().getSightEvent(),
        ticketTaker.getPartner().getSightEvents());

    ticket.setTicketTaker(ticketTaker);
    ticket.setStatus(PUNCHED);
    ticket.setPunchingDate(new Date());

    return ofTicket(ticket);
  }

  @RolesAllowed({ROLE_USHER})
  public TicketDTO findBySerialNumber(CurrentUser currentUser, Long sightEventId,
      String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);
    User ticketTaker = userService.findUserByEmail(currentUser.getPrincipal());

    ticketValidator.validateTicketTakerHasAccessToSightEvent(
        ticket.getTicketPool().getTicketPoolDefinition().getSightEvent(),
        ticketTaker.getPartner().getSightEvents()

    );
    ticketValidator.validateAccessingProperTicket(sightEventId,
        ticket.getTicketPool().getTicketPoolDefinition().getSightEvent().getId());

    return ofTicket(ticket);
  }

  @RolesAllowed({ROLE_USHER, ROLE_EXTERNAL_USER, ROLE_ADMIN})
  public SightEvent findSightEventForTicket(Long id) {
    return ticketDao.findSightEventForTicket(id);
  }


}
