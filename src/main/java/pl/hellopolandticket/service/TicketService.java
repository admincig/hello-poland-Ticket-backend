package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Ticket;

@Stateless
@LocalBean
public class TicketService {

  @Inject
  private TicketDao ticketDao;


  public Ticket save(Ticket ticket) {
    return ticketDao.persist(ticket);
  }

}