package pl.hellopolandticket.service;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.partner.DateType;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.FrequencyType;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;

@Stateless
@LocalBean
public class TicketPoolDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  public TicketPoolDefinitionDTO add(TicketPoolDefinitionDTO ticketPoolDefinitionDTO,
      CurrentUser currentUser) {
    SightEvent sightEvent = sightEventDao.findById(ticketPoolDefinitionDTO.sightEventId);

    FrequencyData frequencyData = ofNullable(ticketPoolDefinitionDTO.frequencyData)
        .map(frequencyDataDTO -> FrequencyData.builder()
            .frequencyType(FrequencyType.valueOf(frequencyDataDTO.frequencyType.name()))
            .dayOfWeek(frequencyDataDTO.dayOfWeek)
            .dayOfMonth(frequencyDataDTO.dayOfMonth)
            .month(frequencyDataDTO.month)
            .frequency(frequencyDataDTO.frequency)
            .build())
        .orElse(null);

    TicketPoolDefinition ticketPoolDefinition = TicketPoolDefinition.builder()
        .name(ticketPoolDefinitionDTO.name)
        .availableTicketsNumber(ticketPoolDefinitionDTO.availableTicketsNumber)
        .cyclicalPool(ticketPoolDefinitionDTO.cyclicalPool)
        .frequencyData(frequencyData)
        .startDate(ticketPoolDefinitionDTO.startDate)
        .endDate(ticketPoolDefinitionDTO.endDate)
        .dateType(DateType.valueOf(ticketPoolDefinitionDTO.dateType.name()))
        .predefinedDate(ticketPoolDefinitionDTO.predefinedDate)
        .date(ticketPoolDefinitionDTO.date)
        .sightEvent(sightEvent)
        .build();

    ticketPoolDefinitionDao.persist(ticketPoolDefinition);

    addAllTicketDefinitionsForTicketPoolDefinition(ticketPoolDefinitionDTO.ticketDefinitions,
        ticketPoolDefinition.getId(), currentUser);

    ticketPoolDefinitionDTO.id = ticketPoolDefinition.getId();

    return ticketPoolDefinitionDTO;
  }

  private List<TicketDefinitionDTO> addAllTicketDefinitionsForTicketPoolDefinition(
      List<TicketDefinitionDTO> ticketDefinitions, Long ticketPoolDefinitionId,
      CurrentUser currentUser) {
    if (ticketDefinitions != null) {
      for (TicketDefinitionDTO ticketDefinitionDTO : ticketDefinitions) {
        ticketDefinitionDTO.ticketPoolDefinitionIds = singletonList(ticketPoolDefinitionId);

        TicketDefinitionDTO persistedTicketDefinitionDTO = ticketDefinitionService
            .add(ticketDefinitionDTO, currentUser);

        ticketDefinitionDTO.id = persistedTicketDefinitionDTO.id;
      }
    }

    return ticketDefinitions;
  }
}
