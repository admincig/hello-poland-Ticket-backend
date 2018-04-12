package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;
import static pl.hellopolandticket.service.validator.TicketValidator.validateAccessingProperTicket;
import static pl.hellopolandticket.service.validator.TicketValidator.validateTicketHasDemandedStatus;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.dto.TicketDTO;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;

  public TicketDTO punchTicket(Long sightId, String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    validateAccessingProperTicket(sightId, ticket.getSight().getId());
    validateTicketHasDemandedStatus(ticket);

    ticket.setStatus(PUNCHED);

    return ofTicket(ticket);
  }

  public TicketDTO findBySerialNumber(Long sightId, String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    validateAccessingProperTicket(sightId, ticket.getSight().getId());

    return ofTicket(ticket);
  }


}