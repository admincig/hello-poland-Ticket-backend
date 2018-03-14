package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.hellopolandticket.model.Ticket;

@Stateless
@LocalBean
public class TicketDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Ticket persist(Ticket ticket) {
    entityManager.persist(ticket);

    return ticket;
  }

}