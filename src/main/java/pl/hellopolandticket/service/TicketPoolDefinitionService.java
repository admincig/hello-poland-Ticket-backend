package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

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
  private TicketDefinitionService ticketDefinitionService;
  @Inject
  private TicketPoolService ticketPoolService;
  @Inject
  private AvailableTicketNumberAssociationService atnaService;

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
    ticketPoolDefinition.setTicketDefinitions(getTicketDefinitions(tdDTOs, ticketPoolDefinition));
    tpdDTO = ModelObjectsToDTOConverter.ofTicketPoolDefinition(ticketPoolDefinition);
    var tds = tpdDTO.ticketDefinitions;
    if (tds != null && !tds.isEmpty()) {
      tds.forEach(td -> td.poolId = ticketPoolDefinition.getId());
    }
    tpdDTO.id = ticketPoolDefinition.getId();
    if (!ticketPoolDefinition.getIsCyclic()) {
      ticketPoolService.findOrCreateNew(ticketPoolDefinition, null);
    }
    return tpdDTO;
  }

  private void validateDates(TicketPoolDefinitionDTO tpdDTO) {
    if (tpdDTO.endDate != null && tpdDTO.startDate.after(tpdDTO.endDate)) {
      throw new ConflictingException("Ticket pool definition's startDate after endDate.");
    }
    if (tpdDTO.entryEndDate != null && tpdDTO.entryStartDate != null
        && tpdDTO.entryStartDate.after(tpdDTO.entryEndDate)) {
      throw new ConflictingException("Ticket pool definition's entryStartDate after entryEndDate.");
    }
    if (tpdDTO.entryStartDate != null && tpdDTO.startDate != null
        && tpdDTO.entryStartDate.after(tpdDTO.startDate)) {
      throw new ConflictingException("Ticket pool definition's entryStartDate after startDate.");
    }
    var frequencyData = tpdDTO.frequencyData;
    if (frequencyData != null && frequencyData.endDate != null
        && tpdDTO.startDate.after(frequencyData.endDate)) {
      throw new ConflictingException("Ticket pool definition's startDate after frequency endDate.");
    }
    if (frequencyData != null && frequencyData.endDate != null && frequencyData.startDate != null
        && frequencyData.startDate.after(frequencyData.endDate)) {
      throw new ConflictingException(
          "Ticket pool definition's frequency startDate after frequency endDate.");
    }
  }

  private List<TicketDefinition> getTicketDefinitions(List<TicketDefinitionDTO> ticketDefinitions,
      TicketPoolDefinition ticketPoolDefinition) {
    if (ticketDefinitions != null) {
      List<TicketDefinition> tickets = new ArrayList<>();
      for (TicketDefinitionDTO ticketDefinitionDTO : ticketDefinitions) {
        TicketDefinition td = ticketDefinitionService.get(ticketDefinitionDTO.id);
        td.getTicketPoolDefinitions().add(ticketPoolDefinition);
        tickets.add(td);
      }
      return tickets;
    }
    return null;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketPoolDefinitionDTO> getAllForPartner(CurrentUser currentUser) {
    List<TicketPoolDefinition> tpd =
        ticketPoolDefinitionDao.findAllByPartner(ofNullable(currentUser.getPrincipal())
            .map(principal -> partnerDao.findByUserEmail(principal)).map(Partner::getId)
            .orElse(null));
    List<TicketPoolDefinitionDTO> dtos = new ArrayList<>();
    for (var d : tpd) {
      List<AvailableTicketNumberAssociation> a = atnaService.getForTicketPoolDefinition(d).stream()
          .filter(p -> p.getTicketDefinition() != null).collect(Collectors.toList());
      var tpdDto = ModelObjectsToDTOConverter.ofTicketPoolDefinition(d);
      tpdDto.ticketDefinitions.forEach(td -> {
        for (var i : a) {
          if (i.getTicketDefinition().getId() == td.id) {
            td.availableTicketsNumber = i.getAvailableTicketsNumber();
            break;
          }
        }
      });
      dtos.add(tpdDto);
    }
    return dtos;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketPoolDefinition get(Long id) {
    return ticketPoolDefinitionDao.findById(id);
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

}
