package pl.hellopolandticket.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.Status.BOUGHT;
import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.ModelObjectsToDTOConverter.ofSightEvent;
import static pl.hellopolandticket.service.dto.SightEventDTO.ofSightEventBasic;
import static pl.hellopolandticket.service.dto.SightEventDTO.ofSightEventWithBoughtAndTotalTickets;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.Location;
import pl.hellopoland.dto.Sight;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.dto.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.dto.SightEventDTO;

@Stateless
@LocalBean
public class SightEventService extends ServiceSuperclass {

  private static final String SIGHT_EVENTS_UPLOAD_URL_PROPERTY = "rest.url.sightEventsUpload";

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private TicketDao ticketDao;

  @Inject
  private HttpClient httpClient;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  public SightEventDTO findById(Long sightEventId) {
    return ofSightEventBasic(sightEventDao.findById(sightEventId));
  }

  public SightEvent findSightEventById(Long sightEventId) {
    return sightEventDao.findById(sightEventId);
  }

  public List<SightEventDTO> findForPartner(String principal) {
    Partner partner = ofNullable(partnerDao.findByName(principal))
        .orElseGet(() -> partnerDao.findByUserEmail(principal));

    return partner.getSightEvents().stream()
        .map(this::toSightEventDTO)
        .collect(toList());
  }

  public List<Sight> findAllAndConvertToDTOObject() {
    List<SightEvent> sightEvents = sightEventDao.findAll();
    sightEvents.forEach(SightEvent::getTicketDefinitions);

    return sightEvents.stream()
        .map(ModelObjectsToDTOConverter::ofSightEvent)
        .collect(toList());
  }

  public Sight addSightEvent(Sight sightEvent, CurrentUser currentUser) {
    SightLocation sightLocation = ofNullable(sightEvent.location)
        .map(this::ofLocation)
        .orElse(null);

    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());

    SightEvent sightEventToPersist = SightEvent.builder()
        .name(sightEvent.name)
        .description(sightEvent.description)
        .mainImageUrl(sightEvent.mainImageUrl)
        .email(sightEvent.email)
        .phone(sightEvent.phone)
        .sightLocation(sightLocation)
        .partner(partner)
        .build();

    sightEventToPersist = sightEventDao.persist(sightEventToPersist);

    Sight persistedSightEvent = ofSightEvent(sightEventToPersist);

    httpClient.sendPostRequest(
        applicationPropertyService.findByName(SIGHT_EVENTS_UPLOAD_URL_PROPERTY).getPropertyValue(),
        singletonList(persistedSightEvent));

    return persistedSightEvent;
  }

  private SightLocation ofLocation(Location location) {
    return SightLocation.builder()
        .latitude(location.latitude)
        .longitude(location.longitude)
        .street(location.street)
        .zipCode(location.zipCode)
        .city(location.city)
        .country(location.country)
        .build();
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
