package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.model.util.UUIDGeneratorUtil.generateUUID;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicket;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketAfterPunch;
import java.util.Date;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.CannotGenerateTicketSerialNumberException;
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
  @Inject
  AvailableTicketNumberAssociationService atnaService;
  @Inject
  SightEventService seService;

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

    return ofTicketAfterPunch(ticket);
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

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void setStatusAsBought(Ticket ticket) {
    ticket.setStatus(BOUGHT);
    generateSerialNumber(ticket);
  }

  public Ticket generateSerialNumber(Ticket ticket) {
    String uuid = generateUUID();
    int i = 0;
    while (!ticketDao.isUniqueSerialNumber(uuid)) {
      i++;
      uuid = generateUUID();
      if (i == 1000) {
        throw new CannotGenerateTicketSerialNumberException();
      }
    }

    ticket.setSerialNumber(uuid);
    return ticket;
  }

}
