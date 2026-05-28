package pl.hellopolandticket.dao;

import java.util.List;
import java.util.Optional;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.hellopolandticket.model.ticket.partner.TicketType;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class TicketTypeDao {

  @PersistenceContext
  private EntityManager entityManager;

  public Optional<TicketType> findById(Long ticketTypeId) {
    return entityManager.createQuery(
        "from TicketType ticketType where ticketType.id = :id",
        TicketType.class)
        .setParameter("id", ticketTypeId)
        .getResultStream()
        .findFirst();
  }

  public List<TicketType> getActiveList() {
    return entityManager.createQuery(
        "from TicketType ticketType where ticketType.active = true order by ticketType.sortOrder asc, ticketType.id asc",
        TicketType.class)
        .getResultList();
  }

}
