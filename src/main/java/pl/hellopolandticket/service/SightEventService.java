package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofSightEvent;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofSightEventBasic;
import java.util.List;
import java.util.stream.Stream;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.LocationDTO;
import pl.hellopoland.dto.PushDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.sightevent.SightEventLocation;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class SightEventService extends ServiceSuperclass {

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private UserService userService;

  public SightEventDTO findById(Long sightEventId) {
    return ofSightEventBasic(sightEventDao.findById(sightEventId));
  }

  public List<SightEventDTO> findByIdsIn(List<Long> sightEventId) {
    return sightEventDao.findBySightEventIdsIn(sightEventId).stream().map(sightEvent -> {
      sightEvent.getTicketPoolDefinitions().size();
      return ofSightEvent(sightEvent, null);
    }).collect(toList());
  }

  public SightEvent findSightEventById(Long sightEventId) {
    return sightEventDao.findById(sightEventId);
  }

  public List<SightEventDTO> findForPartner(String principal) {
    Partner partner = ofNullable(partnerDao.findByName(principal))
        .orElseGet(() -> partnerDao.findByUserEmail(principal));

    return partner.getSightEvents().stream()
        .map(se -> ModelObjectsToDTOConverter.ofSightEvent(se, null)).collect(toList());
  }

  public PushDTO findAllAndConvertToPushDTOObject(CurrentUser currentUser) {
    User user = userService.findUserByEmail(currentUser.getPrincipal());

    Partner partner = user.getPartner();

    List<Long> sightEventIds =
        partner.getSightEvents().stream().map(SightEvent::getId).collect(toList());

    List<SightEvent> sightEvents = sightEventDao.findBySightEventIdsIn(sightEventIds);
    sightEvents.forEach(
        sightEvent -> sightEvent.getTicketPools().forEach(TicketPool::getTicketDefinitions));

    PushDTO sightEventsPushDTO = new PushDTO();

    sightEventsPushDTO.sightEvents =
        sightEvents.stream().map(sightEvent -> ofSightEvent(sightEvent, null)).collect(toList());

    sightEventsPushDTO.secret = user.getToken();

    return sightEventsPushDTO;
  }

  public SightEventDTO addSightEvent(SightEventDTO sightEventDTO, CurrentUser currentUser) {
    SightEventLocation sightEventLocation =
        ofNullable(sightEventDTO.location).map(this::ofLocation).orElse(null);

    User user = userService.findUserByEmail(currentUser.getPrincipal());

    SightEvent sightEventToPersist = SightEvent.builder().name(sightEventDTO.name)
        .description(sightEventDTO.description).mainImageUrl(sightEventDTO.mainImageUrl)
        .email(sightEventDTO.email).phone(sightEventDTO.phone).lead(sightEventDTO.lead)
        .sightEventLocation(sightEventLocation).partner(user.getPartner())
        .generalAdmission(sightEventDTO.generalAdmission).build();

    sightEventToPersist = sightEventDao.persist(sightEventToPersist);

    SightEventDTO persistedSightEvent = ofSightEvent(sightEventToPersist, sightEventDTO.sightId);
    PushDTO sightEventsPushDTO = new PushDTO();

    sightEventsPushDTO.sightEvents = Stream.of(persistedSightEvent).collect(toList());
    sightEventsPushDTO.secret = user.getToken();

    return persistedSightEvent;
  }

  public void delete(Long sightEventId) {
    SightEvent sightEvent = findSightEventById(sightEventId);

    sightEvent.setActive(false);
  }

  private SightEventLocation ofLocation(LocationDTO location) {
    return SightEventLocation.builder().latitude(location.latitude).longitude(location.longitude)
        .street(location.street).zipCode(location.zipCode).city(location.city)
        .country(location.country).build();
  }

  public SightEventDTO updateSightEvent(Long sightId, SightEventDTO sightEventDTO) {
    SightEvent sightEvent = sightEventDao.findById(sightId);

    sightEvent.setName(sightEventDTO.name);
    sightEvent.setDescription(sightEventDTO.description);
    sightEvent.setEmail(sightEventDTO.email);
    sightEvent.setPhone(sightEventDTO.phone);

    sightEvent.setMainImageUrl(sightEventDTO.mainImageUrl);

    return sightEventDTO;
  }
}
