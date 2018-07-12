package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static pl.hellopolandticket.model.ticket.market.Status.BOUGHT;
import static pl.hellopolandticket.model.ticket.market.Status.PUNCHED;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofTicketPool;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketPoolDTO;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.partner.DateType;
import pl.hellopolandticket.model.ticket.partner.TicketPool;

@Stateless
@LocalBean
public class TicketPoolService extends ServiceSuperclass {

  @Inject
  private TicketPoolDao ticketPoolDao;

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private TicketDao ticketDao;

  public TicketPoolDTO add(TicketPoolDTO ticketPoolDTO) {
    SightEvent sightEvent = sightEventDao.findById(ticketPoolDTO.sightEventId);

    TicketPool ticketPool = TicketPool.builder()
        .name(ticketPoolDTO.name)
        .availableTicketsNumber(ticketPoolDTO.availableTicketsNumber)
        .startDate(ticketPoolDTO.startDate)
        .endDate(ticketPoolDTO.endDate)
        .dateType(DateType.valueOf(ticketPoolDTO.dateType.name()))
        .sightEvent(sightEvent)
        .build();

    ticketPoolDao.persist(ticketPool);

    ticketPoolDTO.id = ticketPool.getId();

    return ticketPoolDTO;
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
}
