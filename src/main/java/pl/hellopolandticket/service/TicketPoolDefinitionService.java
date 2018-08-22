package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import java.util.List;
import java.util.stream.Collectors;
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
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class TicketPoolDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  @Inject
  private SightEventDao sightEventDao;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  private TicketPoolService ticketPoolService;

  @Inject
  private PartnerDao partnerDao;

  public TicketPoolDefinitionDTO add(TicketPoolDefinitionDTO ticketPoolDefinitionDTO,
      CurrentUser currentUser) {
    SightEvent sightEvent = sightEventDao.findById(ticketPoolDefinitionDTO.sightEventId);

    FrequencyData frequencyData = ofNullable(ticketPoolDefinitionDTO.frequencyData)
        .map(frequencyDataDTO -> FrequencyData.builder()
            .frequencyType(FrequencyType.valueOf(frequencyDataDTO.frequencyType.name()))
            .daysOfWeek(frequencyDataDTO.daysOfWeek).startDate(frequencyDataDTO.startDate)
            .endDate(frequencyDataDTO.endDate).frequency(frequencyDataDTO.frequency).build())
        .orElse(new FrequencyData());

    TicketPoolDefinition ticketPoolDefinition =
        TicketPoolDefinition.builder().name(ticketPoolDefinitionDTO.name)
            .availableTicketsNumber(ticketPoolDefinitionDTO.availableTicketsNumber)
            .isCyclic(ticketPoolDefinitionDTO.isCyclic).frequencyData(frequencyData)
            .startDate(ticketPoolDefinitionDTO.startDate).endDate(ticketPoolDefinitionDTO.endDate)
            .entryStartDate(ticketPoolDefinitionDTO.entryStartDate)
            .entryEndDate(ticketPoolDefinitionDTO.entryEndDate).sightEvent(sightEvent)
            .deleted(false).build();

    ticketPoolDefinitionDao.persist(ticketPoolDefinition);

    addAllTicketDefinitionsForTicketPoolDefinition(ticketPoolDefinitionDTO.ticketDefinitions,
        ticketPoolDefinition.getId(), currentUser);

    ticketPoolDefinitionDTO.id = ticketPoolDefinition.getId();

    if (!ticketPoolDefinition.getIsCyclic()) {
      ticketPoolService.findOrCreateNew(ticketPoolDefinition, null);
    }

    return ticketPoolDefinitionDTO;
  }

  private List<TicketDefinitionDTO> addAllTicketDefinitionsForTicketPoolDefinition(
      List<TicketDefinitionDTO> ticketDefinitions, Long ticketPoolDefinitionId,
      CurrentUser currentUser) {
    if (ticketDefinitions != null) {
      for (TicketDefinitionDTO ticketDefinitionDTO : ticketDefinitions) {
        TicketDefinitionDTO persistedTicketDefinitionDTO =
            ticketDefinitionService.add(ticketDefinitionDTO, ticketPoolDefinitionId, currentUser);

        ticketDefinitionDTO.id = persistedTicketDefinitionDTO.id;
        ticketDefinitionDTO.poolId = ticketPoolDefinitionId;
      }
    }

    return ticketDefinitions;
  }

  public List<TicketPoolDefinitionDTO> getAllForPartner(CurrentUser currentUser) {
    List<TicketPoolDefinition> tpd =
        ticketPoolDefinitionDao.findAllByPartner(ofNullable(currentUser.getPrincipal())
            .map(principal -> partnerDao.findByUserEmail(principal)).map(Partner::getId)
            .orElse(null));

    return tpd.stream().map(d -> ModelObjectsToDTOConverter.ofTicketPoolDefinition(d))
        .collect(Collectors.toList());
  }

  public TicketPoolDefinition get(Long id) {
    return ticketPoolDefinitionDao.findById(id);
  }

  public TicketPoolDefinitionDTO getForPartner(Long id, CurrentUser currentUser) {
    return ModelObjectsToDTOConverter.ofTicketPoolDefinition(ticketPoolDefinitionDao
        .findByIdForPartner(id, partnerDao.findByUserEmail(currentUser.getPrincipal()).getId()));
  }

  public void deleteTicketPoolDefinition(Long id, CurrentUser currentUser) {
    ticketPoolDefinitionDao.deleteTicketPoolDefinition(id,
        partnerDao.findByUserEmail(currentUser.getPrincipal()).getId());
  }

}
