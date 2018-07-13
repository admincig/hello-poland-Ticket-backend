package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketPool;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketPoolDTO;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

@Stateless
@LocalBean
public class TicketPoolService extends ServiceSuperclass {

  @Inject
  private TicketPoolDao ticketPoolDao;

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  public TicketPoolDTO add(Long ticketPoolDefinitionId, TicketPoolDTO ticketPoolDTO) {
    TicketPoolDefinition ticketPoolDefinition = ticketPoolDefinitionDao
        .findById(ticketPoolDefinitionId);

    TicketPool ticketPool = TicketPool.builder()
        .name(ticketPoolDefinition.getName())
        .availableTicketsNumber(ticketPoolDefinition.getAvailableTicketsNumber())
        .startDate(ticketPoolDefinition.getStartDate())
        .endDate(ticketPoolDefinition.getEndDate())
        .predefinedDate(ticketPoolDefinition.getPredefinedDate())
        .date(ticketPoolDTO.date)
        .dateType(ticketPoolDefinition.getDateType())
        .sightEvent(ticketPoolDefinition.getSightEvent())
        .build();

    ticketPoolDao.persist(ticketPool);

    List<TicketDefinition> ticketDefinitions = ticketPoolDefinition.getTicketDefinitions().stream()
        .map(ticketDefinition -> copyTicketDefinition(ticketDefinition, ticketPool))
        .collect(toList());

    ticketPool.setTicketDefinitions(ticketDefinitions);

    return ofTicketPool(ticketPool);
  }

  private TicketPoolDTO toTicketPoolDTO(TicketPool ticketPool) {
    int totalTicketsNumber = ticketDao
        .countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(ticketPool.getId(),
            asList(BOUGHT, PUNCHED)).intValue();

    int boughtTicketsNumber = ticketDao
        .countTicketsByTicketPoolIdAndTicketStatusInTicketStatuses(ticketPool.getId(),
            singletonList(BOUGHT)).intValue();

    TicketPoolDTO ticketPoolDTO = ofTicketPool(ticketPool);

    ticketPoolDTO.totalTicketsNumber = totalTicketsNumber;
    ticketPoolDTO.boughtTicketNumber = boughtTicketsNumber;

    return ticketPoolDTO;
  }

  private TicketDefinition copyTicketDefinition(TicketDefinition ticketDefinition,
      TicketPool ticketPool) {
    TicketDefinition copiedTicketDefinition = TicketDefinition.builder()
        .name(ticketDefinition.getName())
        .price(ticketDefinition.getPrice())
        .partner(ticketDefinition.getPartner())
        .ticketPool(ticketPool)
        .availableTicketsNumber(ticketDefinition.getAvailableTicketsNumber())
        .build();

    return ticketDefinitionDao.persist(copiedTicketDefinition);
  }
}
