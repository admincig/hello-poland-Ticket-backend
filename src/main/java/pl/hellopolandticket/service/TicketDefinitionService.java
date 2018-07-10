package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.DateType;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.TicketDefinition;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private SightEventService sightEventService;

  public TicketDefinition save(TicketDefinition ticketDefinition) {
    return ticketDefinitionDao.persist(ticketDefinition);
  }

  public pl.hellopoland.dto.TicketDefinition add(
      pl.hellopoland.dto.TicketDefinition ticketDefinitionDTO) {
    SightEvent sightEvent = sightEventService.findSightEventById(ticketDefinitionDTO.sightEventId);

    TicketDefinition ticketDefinition = TicketDefinition.builder()
        .name(ticketDefinitionDTO.name)
        .availableTicketsNumber(ticketDefinitionDTO.availableTicketsNumber)
        .price(ticketDefinitionDTO.price)
        .predefinedDate(ticketDefinitionDTO.predefinedDate)
        .date(ticketDefinitionDTO.date)
        .dateType(DateType.valueOf(ticketDefinitionDTO.dateType.name()))
        .sightEvent(sightEvent)
        .build();

    ticketDefinitionDao.persist(ticketDefinition);

    ticketDefinitionDTO.id = ticketDefinition.getId();
    ticketDefinitionDTO.sightEventId = sightEvent.getId();

    return ticketDefinitionDTO;
  }
}