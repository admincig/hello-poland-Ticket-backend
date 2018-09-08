package pl.hellopolandticket.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.AvailableTicketNumberAssociationDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.AvailableTicketNumberAssociationDao;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class AvailableTicketNumberAssociationService extends ServiceSuperclass {

  @Inject
  private AvailableTicketNumberAssociationDao dao;

  @Inject
  private TicketDefinitionService tdService;

  @Inject
  private TicketPoolDefinitionService tpdService;

  public void add(TicketPoolDefinition ticketPoolDefinition,
      List<TicketDefinitionDTO> ticketDefinitionDtos) {
    if (ticketDefinitionDtos != null && !ticketDefinitionDtos.isEmpty()) {
      for (var tdDto : ticketDefinitionDtos) {
        if (tdDto.availableTicketsNumber != null) {
          var bo = AvailableTicketNumberAssociation.builder()
              .ticketDefinition(tdService.get(tdDto.id)).ticketPoolDefinition(ticketPoolDefinition)
              .availableTicketsNumber(tdDto.availableTicketsNumber).build();
          dao.persiste(bo);
        }
      }
    }
  }

  public List<AvailableTicketNumberAssociationDTO> checkAvailabilityOfTickets(
      Long ticketPoolDefinitionId) {
    var tpd = tpdService.get(ticketPoolDefinitionId);
    List<TicketPool> ticketPools = tpd.getTicketPools();
    if (ticketPools == null || ticketPools.isEmpty()) {
      return getForTicketPoolDefinition(ticketPoolDefinitionId).stream()
          .map(bo -> ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(bo))
          .collect(Collectors.toList());
    } else {
      var availableTicketNumbers = new ArrayList<AvailableTicketNumberAssociation>();
      for (var tp : tpd.getTicketPools()) {
        availableTicketNumbers.addAll(getForTicketPool(tp.getId()));
      }
      return availableTicketNumbers.stream()
          .map(bo -> ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(bo))
          .collect(Collectors.toList());
    }
  }

  private List<AvailableTicketNumberAssociation> getForTicketPool(Long ticketPoolId) {
    return dao.getForTicketPool(ticketPoolId);
  }

  private List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      Long ticketPoolDefinitionId) {
    return dao.getForTicketPoolDefinition(ticketPoolDefinitionId);
  }

}
