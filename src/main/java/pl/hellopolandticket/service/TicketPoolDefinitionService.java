package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightEventDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.FrequencyType;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.util.TicketPoolDefinitionAtnasComparer;

@Stateless
@LocalBean
public class TicketPoolDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;
  @Inject
  private SightEventDao sightEventDao;
  @Inject
  private PartnerDao partnerDao;
  @Inject
  private TicketPoolService ticketPoolService;
  @Inject
  private AvailableTicketNumberAssociationService atnaService;
  @Inject
  private TicketPoolQuantityMonitoringService quantityService;

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketPoolDefinitionDTO add(TicketPoolDefinitionDTO tpdDTO, CurrentUser currentUser) {
    validateDates(tpdDTO);
    List<TicketDefinitionDTO> tdDTOs = tpdDTO.ticketDefinitions;
    if (tdDTOs == null || tdDTOs.isEmpty()) {
      logger.log(Level.ERROR,
          "TicketPoolDefinition [id=" + tpdDTO.id + "] must have ticket definitions");
      throw new BadRequestException("TicketPoolDefinition must have ticket definitions.");
    }
    for (TicketDefinitionDTO td : tdDTOs) {
      if (td.availableTicketsNumber != -1 && tpdDTO.availableTicketsNumber != -1) {
        logger.log(Level.ERROR,
            "Bad availableTicketsNumber limit combination. TicketPoolDefinition [id=" + tpdDTO.id
                + "]; TicketDefinition [id=" + td.id + "]");
        throw new ConflictingException("Bad availableTicketsNumber limit combination.");
      }
    }
    SightEvent sightEvent = sightEventDao.findByIdAndPartner(tpdDTO.sightEventId,
        partnerDao.findByUserEmail(currentUser.getPrincipal()));
    var tpdSd = tpdDTO.startDate;
    FrequencyData frequencyData =
        tpdDTO.isCyclic
            ? ofNullable(tpdDTO.frequencyData)
                .map(frequencyDataDTO -> FrequencyData.builder()
                    .frequencyType(FrequencyType.valueOf(frequencyDataDTO.frequencyType.name()))
                    .daysOfWeek(frequencyDataDTO.daysOfWeek)
                    .startDate(
                        frequencyDataDTO.startDate != null ? frequencyDataDTO.startDate : tpdSd)
                    .endDate(frequencyDataDTO.endDate).frequency(frequencyDataDTO.frequency)
                    .build())
                .orElse(new FrequencyData())
            : null;
    TicketPoolDefinition ticketPoolDefinition = TicketPoolDefinition.builder().name(tpdDTO.name)
        .availableTicketsNumber(tpdDTO.availableTicketsNumber).isCyclic(tpdDTO.isCyclic)
        .frequencyData(frequencyData).startDate(tpdDTO.startDate).endDate(tpdDTO.endDate)
        .entryStartDate(tpdDTO.entryStartDate).entryEndDate(tpdDTO.entryEndDate)
        .sightEvent(sightEvent).deleted(false).wholeDay(tpdDTO.wholeDay).build();
    ticketPoolDefinitionDao.persist(ticketPoolDefinition);
    atnaService.add(ticketPoolDefinition, tdDTOs);
    em.refresh(ticketPoolDefinition);
    tpdDTO = ModelObjectsToDTOConverter.ofTicketPoolDefinition(ticketPoolDefinition);
    var tds = tpdDTO.ticketDefinitions;
    if (tds != null && !tds.isEmpty()) {
      tds.forEach(td -> td.poolId = ticketPoolDefinition.getId());
    }
    tpdDTO.id = ticketPoolDefinition.getId();
    if (!ticketPoolDefinition.getIsCyclic()) {
      ticketPoolService.createNew(ticketPoolDefinition, ticketPoolDefinition.getStartDate());
    }
    return tpdDTO;
  }

  private void validateDates(TicketPoolDefinitionDTO tpdDTO) {
    if (tpdDTO.endDate != null && tpdDTO.startDate.after(tpdDTO.endDate)) {
      logger.log(Level.ERROR,
          "Ticket pool definition's  [id=" + tpdDTO.id + "] startDate after endDate.");
      throw new ConflictingException("Ticket pool definition's startDate after endDate.");
    }
    if (tpdDTO.entryEndDate != null && tpdDTO.entryStartDate != null
        && tpdDTO.entryStartDate.after(tpdDTO.entryEndDate)) {
      logger.log(Level.ERROR,
          "Ticket pool definition's  [id=" + tpdDTO.id + "] entryStartDate after entryEndDate.");
      throw new ConflictingException("Ticket pool definition's entryStartDate after entryEndDate.");
    }
    if (tpdDTO.entryStartDate != null && tpdDTO.startDate != null
        && tpdDTO.entryStartDate.after(tpdDTO.startDate)) {
      logger.log(Level.ERROR,
          "Ticket pool definition's  [id=" + tpdDTO.id + "] entryStartDate after startDate.");
      throw new ConflictingException("Ticket pool definition's entryStartDate after startDate.");
    }
    var frequencyData = tpdDTO.frequencyData;
    if (frequencyData != null && frequencyData.endDate != null
        && tpdDTO.startDate.after(frequencyData.endDate)) {
      logger.log(Level.ERROR,
          "Ticket pool definition's  [id=" + tpdDTO.id + "] startDate after frequency endDate.");
      throw new ConflictingException("Ticket pool definition's startDate after frequency endDate.");
    }
    if (frequencyData != null && frequencyData.endDate != null && frequencyData.startDate != null
        && frequencyData.startDate.after(frequencyData.endDate)) {
      logger.log(Level.ERROR, "Ticket pool definition's  [id=" + tpdDTO.id
          + "] frequency startDate after frequency endDate.");
      throw new ConflictingException(
          "Ticket pool definition's frequency startDate after frequency endDate.");
    }
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketPoolDefinitionDTO> getAllForPartner(CurrentUser currentUser) {
    List<TicketPoolDefinition> tpd =
        ticketPoolDefinitionDao.findAllByPartner(ofNullable(currentUser.getPrincipal())
            .map(principal -> partnerDao.findByUserEmail(principal)).map(Partner::getId)
            .orElse(null));
    List<TicketPoolDefinitionDTO> dtos = new ArrayList<>();
    for (var d : tpd) {
      List<AvailableTicketNumberAssociation> atnas =
          atnaService.getForTicketPoolDefinition(d);
      var tpdDto = ModelObjectsToDTOConverter.ofTicketPoolDefinition(d);
      for (var iter = tpdDto.ticketDefinitions.iterator(); iter.hasNext();) {
        TicketDefinitionDTO td = iter.next();
        for (var atna : atnas) {
          if (atna.getTicketDefinition().getId() == td.id) {
            if (atna.isDeleted()) {
              iter.remove();
            } else if (atna.getTicketPoolDefinition().getAvailableTicketsNumber() == -1) {
              td.availableTicketsNumber = -1;
            } else {
              td.availableTicketsNumber = atna.getAvailableTicketsNumber();
            }
            break;
          }
        }
      }
      dtos.add(tpdDto);
    }
    return dtos;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketPoolDefinitionDTO> update(TicketPoolDefinitionDTO dto,
      CurrentUser currentUser) {
    TicketPoolDefinition tpd = ticketPoolDefinitionDao.findByIdForPartner(dto.id,
        ofNullable(currentUser.getPrincipal())
            .map(principal -> partnerDao.findByUserEmail(principal)).map(Partner::getId)
            .orElse(null));

    tpd.setName(dto.name);
    int oldAvailableTicketsNumber = tpd.getAvailableTicketsNumber();
    tpd.setAvailableTicketsNumber(dto.availableTicketsNumber);
    updateAtnas(tpd, dto);
    List<TicketPool> pools =
        ticketPoolService.updatePoolsAvailableTicketsNumber(tpd, oldAvailableTicketsNumber);
    quantityService.informPartnerAboutPoolsRunningOut(pools.stream());
    return getAllForPartner(currentUser);
  }

  private void updateAtnas(TicketPoolDefinition tpd, TicketPoolDefinitionDTO dto) {
    var diffs = new TicketPoolDefinitionAtnasComparer(tpd).getDifferences(dto);
    new TicketPoolDefinitionAtnasDiffApplier(atnaService).apply(diffs);

    var toInform = Stream.concat(
        diffs.toRemove.stream(),
        diffs.toModify.stream().map(Entry::getKey))
        .flatMap(atna -> atna.getChildren().stream())
        .distinct();

    quantityService.informPartnerAboutAtnasRunningOut(toInform);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketPoolDefinitionDTO getForPartner(Long id, CurrentUser currentUser) {
    return ModelObjectsToDTOConverter.ofTicketPoolDefinition(ticketPoolDefinitionDao
        .findByIdForPartner(id, partnerDao.findByUserEmail(currentUser.getPrincipal()).getId()));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void deleteTicketPoolDefinition(Long id, CurrentUser currentUser) {
    ticketPoolDefinitionDao.deleteTicketPoolDefinition(id,
        partnerDao.findByUserEmail(currentUser.getPrincipal()).getId());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketPoolDefinition> getAvailable(Set<Long> sightEventIds, Date fromDate,
      Date toDate) {
    boolean anyId = sightEventIds != null && !sightEventIds.isEmpty();
    var queryStr = new StringBuilder("from TicketPoolDefinition where deleted is false ");
    if (anyId) {
      queryStr.append("and sightEvent.id in (:sightEventIds)");
    }

    queryStr.append(
        " and ((isCyclic is true and (startDate > :fromDate or (frequencyData.endDate is not null and frequencyData.endDate > :fromDate) or frequencyData.endDate is null)");
    if (toDate != null) {
      queryStr.append(" and :toDate > startDate");
    }
    queryStr.append(
        ") or (isCyclic is false and (startDate > :fromDate or (wholeDay is true and date_trunc('day', startDate) = to_date(:fromDateToDay, 'YYYY-MM-DD')))");
    if (toDate != null) {
      queryStr.append(" and :toDate > startDate");
    }
    queryStr.append("))");
    var query = em.createQuery(queryStr.toString(), TicketPoolDefinition.class)
        .setParameter("fromDate", fromDate != null ? fromDate : new Date())
        .setParameter("fromDateToDay",
            fromDate != null
                ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString()
                : LocalDate.now().toString());
    if (anyId) {
      query.setParameter("sightEventIds", sightEventIds);
    }
    if (toDate != null) {
      query.setParameter("toDate", toDate);
    }
    return query.getResultList();
  }

  @RolesAllowed({ROLE_ADMIN})
  public void repairEntryDates(List<TicketPoolDefinition> tpds) {
    tpds.forEach(tpd -> repairEntryDates(tpd));
  }

  // @formatter:off
  /**
   * kryteria na nowe pule wygladaja tak:
   *  1. start_date i end_date beda roznily sie czasem
   *  2. entry_end_date musi byc takie samo jak end_date
   *  3. entry_start_date moze miec rozny czas w stosunku do start_date o 0, 15, 30 i 60 minut wczesniej
   * ----- 
   * cala reszte modyfikujemy w taki sposob, ze entry_start_date = start_date i entry_end_date = end_date
   **/
  // @formatter:on
  @SuppressWarnings("deprecation")
  public void repairEntryDates(TicketPoolDefinition tpd) {
    var startDate = tpd.getStartDate();
    var endDate = tpd.getEndDate();
    // 1.
    if (endDate != null && !startDate.toInstant().atZone(ZoneId.systemDefault())
        .truncatedTo(ChronoUnit.DAYS)
        .equals(endDate.toInstant().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS))) {
      endDate.setYear(startDate.getYear());
      endDate.setMonth(startDate.getMonth());
      endDate.setDate(startDate.getDate());
      tpd.setEndDate(endDate);
      tpd.getTicketPools().forEach(tp -> tp.setEndDate(endDate));
    }

    // 2.
    var eeDate = tpd.getEntryEndDate();
    if (eeDate != null && !eeDate.equals(endDate)) {
      tpd.setEntryEndDate(endDate);
      tpd.getTicketPools().forEach(tp -> tp.setEntryEndDate(endDate));
    }

    // 3.
    var esDate = tpd.getEntryStartDate();
    if (esDate != null) {
      long sTime = startDate.getTime();
      long esTime = esDate.getTime();
      long range = sTime - esTime;
      boolean wrongRange = !(range == 0 || range == (15 * 60 * 1000) || range == (30 * 60 * 1000)
          || range == (60 * 60 * 1000));

      if (wrongRange) {
        tpd.setEntryStartDate(startDate);
        tpd.getTicketPools().forEach(tp -> tp.setEntryStartDate(startDate));
      }
    }
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketPoolDefinitionDTO> getWholeDay(List<Long> tpdIds) {
    return ticketPoolDefinitionDao.getWholeDay(tpdIds).stream()
        .map(ModelObjectsToDTOConverter::ofTicketPoolDefinition).collect(Collectors.toList());
  }

}
