package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.TicketDefinition;

@Stateless
@LocalBean
public class TicketDefinitionService {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;


  public TicketDefinition save(TicketDefinition ticketDefinition) {
    return ticketDefinitionDao.persist(ticketDefinition);
  }

}