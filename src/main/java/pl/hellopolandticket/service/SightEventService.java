package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.Status.BOUGHT;
import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.SightEventDTO.ofSightEventBasic;
import static pl.hellopolandticket.service.dto.SightEventDTO.ofSightEventWithBoughtAndTotalTickets;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.service.dto.SightEventDTO;

@Stateless
@LocalBean
public class SightEventService {

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private TicketDao ticketDao;

  public SightEventDTO findById(Long sightEventId) {
    return ofSightEventBasic(sightEventDao.findById(sightEventId));
  }

  public SightEvent findSightEventById(Long sightEventId) {
    return sightEventDao.findById(sightEventId);
  }

  public List<SightEventDTO> findForPartner(String userLogin) {
    Partner partner = partnerDao.findByUserEmail(userLogin);

    List<Long> sightIds = partner.getSights().stream()
        .map(Sight::getId)
        .collect(toList());

    return sightEventDao.findBySightIdsIn(sightIds).stream()
        .map(this::toSightEventDTO)
        .collect(toList());
  }

  private SightEventDTO toSightEventDTO(SightEvent sightEvent) {
    int totalTicketsNumber = ticketDao
        .countTicketsBySightEventIdAndTicketStatusInTicketStatuses(sightEvent.getId(),
            asList(BOUGHT, PUNCHED)).intValue();

    int boughtTicketsNumber = ticketDao
        .countTicketsBySightEventIdAndTicketStatusInTicketStatuses(sightEvent.getId(),
            singletonList(BOUGHT)).intValue();

    return ofSightEventWithBoughtAndTotalTickets(sightEvent, boughtTicketsNumber,
        totalTicketsNumber);
  }


}
