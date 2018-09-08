package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import java.util.ArrayList;
import java.util.List;
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

  @Inject
  private AvailableTicketNumberAssociationService atnaService;

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

    ticketPoolDefinition
        .setTicketDefinitions(getTicketDefinitions(ticketPoolDefinitionDTO.ticketDefinitions));

    ticketPoolDefinitionDao.persist(ticketPoolDefinition);
    atnaService.add(ticketPoolDefinition, ticketPoolDefinitionDTO.ticketDefinitions);

    ticketPoolDefinitionDTO =
        ModelObjectsToDTOConverter.ofTicketPoolDefinition(ticketPoolDefinition);

    var tds = ticketPoolDefinitionDTO.ticketDefinitions;
    if (tds != null && !tds.isEmpty()) {
      tds.forEach(td -> td.poolId = ticketPoolDefinition.getId());
    }

    ticketPoolDefinitionDTO.id = ticketPoolDefinition.getId();

    if (!ticketPoolDefinition.getIsCyclic()) {
      ticketPoolService.findOrCreateNew(ticketPoolDefinition, null);
    }
    return ticketPoolDefinitionDTO;
  }

  private List<TicketDefinition> getTicketDefinitions(List<TicketDefinitionDTO> ticketDefinitions) {
    if (ticketDefinitions != null) {
      List<TicketDefinition> tickets = new ArrayList<>();
      for (TicketDefinitionDTO ticketDefinitionDTO : ticketDefinitions) {
        tickets.add(ticketDefinitionService.get(ticketDefinitionDTO.id));
      }
      return tickets;
    }
    return null;
  }

  public List<TicketPoolDefinitionDTO> getAllForPartner(CurrentUser currentUser) {
    List<TicketPoolDefinition> tpd =
        ticketPoolDefinitionDao.findAllByPartner(ofNullable(currentUser.getPrincipal())
            .map(principal -> partnerDao.findByUserEmail(principal)).map(Partner::getId)
            .orElse(null));

    List<TicketPoolDefinitionDTO> dtos = new ArrayList<>();
    for (var d : tpd) {
      dtos.add(ModelObjectsToDTOConverter.ofTicketPoolDefinition(d));
    }
    return dtos;
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
