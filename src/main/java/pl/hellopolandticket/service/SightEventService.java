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
import java.util.stream.Stream;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import pl.hellopoland.dto.Location;
import pl.hellopoland.dto.Push;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.dto.SightEventDTO;
import pl.hellopolandticket.service.event.HPLPushEvent;

@Slf4j
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

  @Inject
  private UserService userService;

  @Inject
  private Event<HPLPushEvent> hplPushEvent;

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

  public Push findAllAndConvertToPushDTOObject(CurrentUser currentUser) {
    User user = userService.findUserByEmail(currentUser.getPrincipal());

    Partner partner = user.getPartner();

    List<Long> sightEventIds = partner.getSightEvents().stream()
        .map(SightEvent::getId)
        .collect(toList());

    List<SightEvent> sightEvents = sightEventDao.findBySightEventIdsIn(sightEventIds);
    sightEvents.forEach(SightEvent::getTicketDefinitions);

    Push sightEventsPushDTO = new Push();

    sightEventsPushDTO.sightEvents = sightEvents.stream()
        .map(sightEvent -> ofSightEvent(sightEvent, null))
        .collect(toList());

    sightEventsPushDTO.secret = user.getToken();

    return sightEventsPushDTO;
  }

  public pl.hellopoland.dto.SightEvent addSightEvent(
      pl.hellopoland.dto.SightEvent sightEventDTO,
      CurrentUser currentUser) {
    SightLocation sightLocation = ofNullable(sightEventDTO.location)
        .map(this::ofLocation)
        .orElse(null);

    User user = userService.findUserByEmail(currentUser.getPrincipal());

    SightEvent sightEventToPersist = SightEvent.builder()
        .name(sightEventDTO.name)
        .date(sightEventDTO.date)
        .description(sightEventDTO.description)
        .mainImageUrl(ofNullable(sightEventDTO.mainImage)
            .map(mainImage -> mainImage.original)
            .orElse(null))
        .email(sightEventDTO.email)
        .phone(sightEventDTO.phone)
        .sightLocation(sightLocation)
        .partner(user.getPartner())
        .generalAdmission(sightEventDTO.generalAdmission)
        .build();

    sightEventToPersist = sightEventDao.persist(sightEventToPersist);

    pl.hellopoland.dto.SightEvent persistedSightEvent = ofSightEvent(sightEventToPersist,
        sightEventDTO.sightId);
    Push sightEventsPushDTO = new Push();

    sightEventsPushDTO.sightEvents = Stream.of(persistedSightEvent).collect(toList());
    sightEventsPushDTO.secret = user.getToken();

    return persistedSightEvent;
  }

  public void delete(Long sightEventId) {
    SightEvent sightEvent = findSightEventById(sightEventId);

    sightEvent.setActive(false);
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

  public pl.hellopoland.dto.SightEvent updateSightEvent(Long sightId,
      pl.hellopoland.dto.SightEvent sightEventDTO) {
    SightEvent sightEvent = sightEventDao.findById(sightId);

    sightEvent.setName(sightEventDTO.name);
    sightEvent.setDate(sightEventDTO.date);
    sightEvent.setDescription(sightEventDTO.description);
    sightEvent.setEmail(sightEventDTO.email);
    sightEvent.setPhone(sightEventDTO.phone);

    sightEvent.setMainImageUrl(ofNullable(sightEventDTO.mainImage)
        .map(s -> s.original)
        .orElse(null));

    return sightEventDTO;
  }
}
