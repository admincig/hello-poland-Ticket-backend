package pl.hellopolandticket.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.AvailableTicketNumberAssociationDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.AvailableTicketNumberAssociationDao;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
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

  public void add(TicketPool pool, TicketPoolDefinition ticketPoolDefinition) {
    var tds = ticketPoolDefinition.getTicketDefinitions();
    if (tds != null && !tds.isEmpty()) {
      for (TicketDefinition td : tds) {
        AvailableTicketNumberAssociation association =
            dao.findForTicketPoolDefinitionAndTicketDefinition(ticketPoolDefinition, td);
        var bo = AvailableTicketNumberAssociation.builder().ticketDefinition(td).ticketPool(pool)
            .availableTicketsNumber(association.getAvailableTicketsNumber()).build();
        dao.persiste(bo);
      }
    }

  }

  public List<AvailableTicketNumberAssociationDTO> checkAvailabilityOfTickets(
      Long ticketPoolDefinitionId, Date date) {
    var tpd = tpdService.get(ticketPoolDefinitionId);
    List<TicketPool> ticketPools = tpd.getTicketPools();
    if (ticketPools == null || ticketPools.isEmpty()) {
      return getForTicketPoolDefinition(tpd).stream()
          .map(bo -> ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(bo))
          .collect(Collectors.toList());
    } else {
      var availableTicketNumbers = new ArrayList<AvailableTicketNumberAssociation>();
      tpd.getTicketPools().stream().filter(p -> areDatesEquals(date, p.getStartDate()))
          .forEach(tp -> availableTicketNumbers.addAll(getForTicketPool(tp)));
      return availableTicketNumbers.stream()
          .map(bo -> ModelObjectsToDTOConverter.ofAvailableTicketNumberAssociation(bo))
          .collect(Collectors.toList());
    }
  }

  private boolean areDatesEquals(Date date1, Date date2) {
    return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        .isEqual(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
  }

  private List<AvailableTicketNumberAssociation> getForTicketPool(TicketPool tp) {
    return dao.getForTicketPool(tp);
  }

  private List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    return dao.getForTicketPoolDefinition(tpd);
  }

}
