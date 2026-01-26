package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofSightEvent;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofSightEventBasic;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.hellopoland.dto.FileDescriptorDTO;
import pl.hellopoland.dto.LocationDTO;
import pl.hellopoland.dto.OpeningHoursDTO;
import pl.hellopoland.dto.PushDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopoland.dto.SightEventPriceDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.sightevent.OpeningHours;
import pl.hellopolandticket.model.sightevent.PdfAttachment;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.sightevent.SightEventLocation;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
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

  @Inject
  private OpeningHoursService oHoursService;

  @Inject
  private TicketPoolService ticketPoolService;

  @Inject
  private TicketPoolDefinitionService ticketPoolDefService;

  @Inject
  private AvailableTicketNumberAssociationService atnaService;

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public SightEventDTO findById(Long sightEventId) {
    return ofSightEventBasic(sightEventDao.findById(sightEventId));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<SightEventDTO> findByIdsIn(List<Long> sightEventId) {
    return sightEventDao.findBySightEventIdsIn(sightEventId).stream().map(sightEvent -> {
      sightEvent.getTicketPoolDefinitions().size();
      return ofSightEvent(sightEvent, null);
    }).collect(toList());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public SightEvent findSightEventById(Long sightEventId) {
    return sightEventDao.findById(sightEventId);
  }


  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_USHER})
  public List<SightEventDTO> findForPartner(String principal) {
    Partner partner = ofNullable(partnerDao.findByName(principal))
        .orElseGet(() -> partnerDao.findByUserEmail(principal));

    /*return partner.getSightEvents().stream()
            .filter(se -> Boolean.TRUE.equals(se.getActive()))
            .filter(se -> Boolean.TRUE.equals(se.getPublished()))
            .map(se -> ModelObjectsToDTOConverter.ofSightEvent(se, null)).collect(toList());
    */
      return partner.getSightEvents().stream()
              .filter(se -> Boolean.TRUE.equals(se.getActive()))
              .filter(se -> Boolean.TRUE.equals(se.getPublished()))
              .filter(se -> Boolean.FALSE.equals(se.getBlocked()))
              .filter(se ->
                      se.getTicketPoolDefinitions() != null
                              && se.getTicketPoolDefinitions().stream()
                              .anyMatch(tpd ->
                                      !tpd.isDeleted()
                                              && tpd.getTicketPools() != null
                                              && !tpd.getTicketPools().isEmpty()
                              )
              )
              .map(se -> ModelObjectsToDTOConverter.ofSightEvent(se, null))
              .collect(toList());
  }

/*
    @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_USHER})
    public List<SightEventDTO> findForPartner(String principal) {
        Partner partner = ofNullable(partnerDao.findByName(principal))
                .orElseGet(() -> partnerDao.findByUserEmail(principal));

        // Widok zgodny z partnerem: konfiguracja (TPD + TicketDefinitions),
        // bez ticket_pools i bez pokazywania deleted TPD.
        List<SightEvent> sightEvents = em.createQuery(
                        "select distinct se from SightEvent se "
                                + " join fetch se.ticketPoolDefinitions tpd "
                                + " join fetch tpd.ticketDefinitions td "
                                + " where se.partner = :partner "
                                + "   and tpd.deleted = false",
                        SightEvent.class)
                .setParameter("partner", partner)
                .getResultList();

        return sightEvents.stream()
                .map(se -> ModelObjectsToDTOConverter.ofSightEvent(se, null))
                .collect(toList());
    }
*/

    public PushDTO findAllAndConvertToPushDTOObject(CurrentUser currentUser) {
    User user = userService.findUserByEmail(currentUser.getPrincipal());

    Partner partner = user.getPartner();

    List<Long> sightEventIds =
        partner.getSightEvents().stream().map(SightEvent::getId).collect(toList());

    List<SightEvent> sightEvents = sightEventDao.findBySightEventIdsIn(sightEventIds);
    sightEvents.forEach(sightEvent -> sightEvent.getTicketPoolDefinitions()
        .forEach(TicketPoolDefinition::getTicketDefinitions));

    PushDTO sightEventsPushDTO = new PushDTO();

    sightEventsPushDTO.sightEvents =
        sightEvents.stream().map(sightEvent -> ofSightEvent(sightEvent, null)).collect(toList());

    sightEventsPushDTO.secret = user.getToken();

    return sightEventsPushDTO;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public SightEventDTO addSightEvent(SightEventDTO sightEventDTO, CurrentUser currentUser) {
    SightEventLocation sightEventLocation =
        ofNullable(sightEventDTO.location).map(this::ofLocation).orElse(null);

    User user = userService.findUserByEmail(currentUser.getPrincipal());
    String mainImageUrl = null;
    if (sightEventDTO.mainImage != null) {
      mainImageUrl = sightEventDTO.mainImage.original;
    }
    SightEvent sightEventToPersist = sightEventDao.persist(
        SightEvent.builder().name(sightEventDTO.name).description(sightEventDTO.description)
            .mainImageUrl(mainImageUrl).email(sightEventDTO.email).phone(sightEventDTO.phone)
            .lead(sightEventDTO.lead).sightEventLocation(sightEventLocation)
            .partner(user.getPartner()).generalAdmission(sightEventDTO.generalAdmission)
            .published(sightEventDTO.published).blocked(sightEventDTO.blocked).build());

    ArrayList<OpeningHours> oHoursList = getOpeningHoursCollectionFromDTO(sightEventDTO);
    if (oHoursList != null && !oHoursList.isEmpty()) {
      oHoursList.stream().forEach(oh -> {
        oh.setSightEvent(sightEventToPersist);
        oHoursService.persist(oh);
      });
      sightEventToPersist.setOpeningHours(oHoursList);
    }

    SightEventDTO persistedSightEvent = ofSightEvent(sightEventToPersist, sightEventDTO.sightId);
    PushDTO sightEventsPushDTO = new PushDTO();

    sightEventsPushDTO.sightEvents = Stream.of(persistedSightEvent).collect(toList());
    sightEventsPushDTO.secret = user.getToken();

    return persistedSightEvent;
  }

  private ArrayList<OpeningHours> getOpeningHoursCollectionFromDTO(SightEventDTO dto) {
    return ofNullable(dto.openingHours).map(
        l -> l.stream().map(this::ofOpeningHours).collect(Collectors.toCollection(ArrayList::new)))
        .orElse(null);
  }

  private OpeningHours ofOpeningHours(OpeningHoursDTO dto) {
    return OpeningHours.builder().closeTime(dto.closeTime).day(dto.day).openTime(dto.openTime)
        .build();
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void delete(Long sightEventId) {
    SightEvent sightEvent = findSightEventById(sightEventId);
    sightEvent.setActive(false);
  }

  private SightEventLocation ofLocation(LocationDTO location) {
    return SightEventLocation.builder().latitude(location.latitude).longitude(location.longitude)
        .street(location.street).zipCode(location.zipCode).city(location.city)
        .country(location.country).directions(location.directions).build();
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public SightEventDTO updateSightEvent(Long sightId, SightEventDTO sightEventDTO) {
    SightEvent sightEvent = sightEventDao.findById(sightId);
    sightEvent.setDescription(sightEventDTO.description);
    sightEvent.setEmail(sightEventDTO.email);
    sightEvent.setPhone(sightEventDTO.phone);
    if (sightEventDTO.name != null) {
      sightEvent.setName(sightEventDTO.name);
    }
    if (sightEventDTO.blocked != null) {
      sightEvent.setBlocked(sightEventDTO.blocked);
    }
    if (sightEventDTO.published != null) {
      sightEvent.setPublished(sightEventDTO.published);
    }
    String mainImageUrl = null;
    if (sightEventDTO.mainImage != null) {
      mainImageUrl = sightEventDTO.mainImage.original;
    }
    sightEvent.setMainImageUrl(mainImageUrl);

    oHoursService.remove(sightEvent.getOpeningHours());
    ArrayList<OpeningHours> oHoursList = getOpeningHoursCollectionFromDTO(sightEventDTO);
    if (oHoursList != null && !oHoursList.isEmpty()) {
      oHoursList.stream().forEach(oh -> {
        oh.setSightEvent(sightEvent);
        oHoursService.persist(oh);
      });
    }
    sightEvent.setOpeningHours(null);
    sightEvent.setOpeningHours(oHoursList);

    return sightEventDTO;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void stopSale(Long sightEventId, Long ticketPoolDefId, Date date) {
    var sightEvent = sightEventDao.findById(sightEventId);
    TicketPoolDefinition tpd = sightEvent.getTicketPoolDefinitions().stream()
        .filter(t -> !t.isDeleted() && t.getId().equals(ticketPoolDefId)).findFirst().orElseThrow();
    TicketPool tp = null;
    date = setStartDateTimeToRequestedDate(date, tpd);
    if (tpd.getIsCyclic()) {
      tp = ticketPoolService.find(tpd, date);
      if (tp == null) {
        tp = ticketPoolService.createNew(tpd, date);
      }
    } else {
      tp = ticketPoolService.find(tpd, date);
    }
    tp.setAvailableTicketsNumber(0);
    for (var atna : atnaService.getForTicketPool(tp)) {
      atna.setAvailableTicketsNumber(0);
      atnaService.update(atna);
    }
  }

  private Date setStartDateTimeToRequestedDate(Date date, TicketPoolDefinition tpd) {
    LocalTime startDateLocalTime =
        LocalTime.ofInstant(tpd.getStartDate().toInstant(), ZoneId.systemDefault());
    LocalDate dateLocalDate = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
    LocalDateTime dateLocalDateTime = dateLocalDate.atTime(startDateLocalTime);
    return Date.from(dateLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public SightEventDTO uploadPdf(Long sightEventId, FileDescriptorDTO pdf) {
    var se = sightEventDao.findById(sightEventId);
      PdfAttachment a = new PdfAttachment();
      a.setPath(pdf.path);
      a.setOriginalName(pdf.originalName);
      se.setPdfAttachmentsPaths(Set.of(a));


      return ofSightEventBasic(se);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void removePdf(Long sightEventId, String path) {
    var se = sightEventDao.findById(sightEventId);
    // temporary only one pdf for SightEvent:
    // se.getPdfAttachmentsPaths().remove(path);
    se.setPdfAttachmentsPaths(Set.of());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public Set<Long> getAvailableSightEventsIds(Set<Long> sightEventIds) {
    var result = new HashSet<Long>();
    var notCyclicTPDs = new HashSet<TicketPoolDefinition>();
    ticketPoolDefService.getAvailable(sightEventIds, null, null).forEach(tpd -> {
      if (tpd.getIsCyclic()) {
        result.add(tpd.getSightEvent().getId());
      } else {
        notCyclicTPDs.add(tpd);
      }
    });
    var associations = new ArrayList<AvailableTicketNumberAssociation>();
    notCyclicTPDs.forEach(tpd -> associations.addAll(
        atnaService.getAvailabilityOfTicketsForNonCyclicTicketPool(tpd.getTicketPools().get(0))));
    associations.forEach(
        a -> result.add(a.getTicketPool().getTicketPoolDefinition().getSightEvent().getId()));
    return result;
  }

    @RolesAllowed({ROLE_EXTERNAL_USER})
    public Set<SightEventPriceDTO> getSightEventsIdsInDateRange(Set<Long> sightEventIds,
                                                                Date fromDate, Date toDate) {

        Map<Long, SightEventPriceDTO> result = new HashMap<>();

        ticketPoolDefService.getAvailable(sightEventIds, fromDate, toDate)
                .stream()
                .collect(Collectors.groupingBy(tpd -> tpd.getSightEvent().getId()))
                .forEach((sightEventId, tpds) -> {

                    SightEventPriceDTO dto = new SightEventPriceDTO();
                    dto.id = sightEventId;
                    dto.price = Integer.MAX_VALUE;

                    for (var tpd : tpds) {
                        var best = tpd.getTicketDefsWithAtna().stream()
                                .min(Comparator.comparing(x -> x.ticketDefinition().getPrice()));

                        if (best.isEmpty()) continue;

                        var td = best.get().ticketDefinition();
                        var atna = best.get().atna();

                        if (td.getPrice() < dto.price) {
                            dto.price = td.getPrice();
                            dto.discountPrice = (atna.getDiscount() != null)
                                    ? atna.getDiscount().getDiscountPrice()
                                    : null;
                        }
                    }

                    result.put(dto.id, dto);
                });

        return new HashSet<>(result.values());
    }


}
