package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  @Inject
  private TicketPoolDao ticketPoolDao;


  public TicketDefinitionDTO add(TicketDefinitionDTO ticketDefinitionDTO, CurrentUser currentUser) {
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());

    TicketPool ticketPool = ofNullable(ticketDefinitionDTO.ticketPoolId)
        .map(ticketPoolId -> ticketPoolDao.findById(ticketPoolId))
        .orElse(null);

    List<TicketPoolDefinition> ticketPoolDefinitions = ofNullable(
        ticketDefinitionDTO.ticketPoolDefinitionIds)
        .map(
            ticketPoolDefinitionIds -> ticketPoolDefinitionDao.findByIdsIn(ticketPoolDefinitionIds))
        .orElse(null);

    TicketDefinition ticketDefinition = TicketDefinition.builder()
        .name(ticketDefinitionDTO.name)
        .price(ticketDefinitionDTO.price)
        .partner(partner)
        .availableTicketsNumber(ticketDefinitionDTO.availableTicketsNumber)
        .ticketPool(ticketPool)
        .ticketPoolDefinitions(ticketPoolDefinitions)
        .build();

    ticketDefinitionDao.persist(ticketDefinition);

    ticketDefinitionDTO.id = ticketDefinition.getId();

    return ticketDefinitionDTO;
  }
}