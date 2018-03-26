package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.TicketStatus.BOUGHT;
import static pl.hellopolandticket.model.TicketStatus.PUNCHED;
import static pl.hellopolandticket.service.dto.SightDTO.ofSightWithBoughtAndTotalTickets;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.SightDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.service.dto.SightDTO;

@Stateless
@LocalBean
public class SightService {

  @Inject
  private SightDao sightDao;

  @Inject
  private TicketDao ticketDao;

  public Sight save(Sight sight) {
    return sightDao.persist(sight);
  }

  public Sight findBySightName(String sightName) {
    return sightDao.findBySightName(sightName);
  }

  public SightDTO findById(Long sightId) {
    return toSightDTO(sightDao.findById(sightId));
  }

  public List<SightDTO> findAll() {
    return sightDao.findAll().stream()
        .map(this::toSightDTO)
        .collect(toList());
  }

  private SightDTO toSightDTO(Sight sight) {
    int totalTicketsNumber = ticketDao
        .countTicketsBySightIdAndTicketStatusInTicketStatuses(sight.getId(),
            asList(BOUGHT, PUNCHED)).intValue();

    int boughtTicketsNumber = ticketDao
        .countTicketsBySightIdAndTicketStatusInTicketStatuses(sight.getId(),
            singletonList(BOUGHT)).intValue();

    return ofSightWithBoughtAndTotalTickets(sight, boughtTicketsNumber, totalTicketsNumber);
  }
}