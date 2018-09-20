package pl.hellopolandticket.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import pl.hellopolandticket.service.exception.preconditionfailed.CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class AvailableTicketNumberAssociationService extends ServiceSuperclass {

  @Inject
  private AvailableTicketNumberAssociationDao dao;

  @Inject
  private SightEventService seService;

  @Inject
  private TicketDefinitionService tdService;

  @Inject
  private TicketPoolService tpService;

  @Inject
  private TicketPoolDefinitionService tpdService;

  public void add(TicketPoolDefinition ticketPoolDefinition,
      List<TicketDefinitionDTO> ticketDefinitionDtos) {
    if (ticketDefinitionDtos != null && !ticketDefinitionDtos.isEmpty()) {
      for (var tdDto : ticketDefinitionDtos) {
        var bo = AvailableTicketNumberAssociation.builder()
            .ticketDefinition(tdService.get(tdDto.id)).ticketPoolDefinition(ticketPoolDefinition)
            .availableTicketsNumber(
                tdDto.availableTicketsNumber != null ? tdDto.availableTicketsNumber
                    : ticketPoolDefinition.getAvailableTicketsNumber())
            .build();
        dao.persist(bo);
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
        dao.persist(bo);
      }
    }

  }

  public AvailableTicketNumberAssociationDTO checkAvailabilityOfTickets(Long sightEventId,
      Date date) {
    var se = seService.findSightEventById(sightEventId);
    se.getTicketPoolDefinitions().size();

    var associationsTPD = new ArrayList<AvailableTicketNumberAssociation>();
    var associationsTP = new ArrayList<AvailableTicketNumberAssociation>();

    se.getTicketPoolDefinitions().stream().filter(tpd -> checkDates(date, tpd)).forEach(tpd -> {
      List<TicketPool> ticketPools = tpd.getTicketPools();
      tpd.getTicketPools().size();
      if (ticketPools == null || ticketPools.isEmpty()) {
        associationsTPD.addAll(getForTicketPoolDefinition(tpd));
      } else {
        var availableTicketNumbers = new ArrayList<AvailableTicketNumberAssociation>();



        ticketPools.stream().filter(p -> {
          boolean b = p.getStartDate().getTime() == date.getTime();
          return b;
          // return areDatesEquals(p.getStartDate(), date);
        }).forEach(tp -> availableTicketNumbers.addAll(getForTicketPool(tp)));



        associationsTP.addAll(availableTicketNumbers);
      }
    });

    var result = new AvailableTicketNumberAssociationDTO();
    result.ticketPools = new ArrayList<>();

    Map<TicketPoolDefinition, List<AvailableTicketNumberAssociation>> grupedByTPD =
        associationsTPD.stream().collect(Collectors.groupingBy(a -> a.getTicketPoolDefinition()));
    for (Entry<TicketPoolDefinition, List<AvailableTicketNumberAssociation>> entry : grupedByTPD
        .entrySet()) {
      var tpdDTO = ModelObjectsToDTOConverter.ofTicketPoolDefinitionBasic(entry.getKey());
      var tdDTOs = new ArrayList<TicketDefinitionDTO>();
      for (AvailableTicketNumberAssociation a : entry.getValue()) {
        var tdDTO = ModelObjectsToDTOConverter.ofTicketDefinition(a.getTicketDefinition());
        tdDTO.availableTicketsNumber = a.getAvailableTicketsNumber();
        tdDTOs.add(tdDTO);
      }
      tpdDTO.ticketDefinitions = tdDTOs;
      result.ticketPools.add(tpdDTO);
    }

    Map<TicketPool, List<AvailableTicketNumberAssociation>> grupedByTP =
        associationsTP.stream().collect(Collectors.groupingBy(a -> a.getTicketPool()));
    for (Entry<TicketPool, List<AvailableTicketNumberAssociation>> entry : grupedByTP.entrySet()) {
      var tpDTO = ModelObjectsToDTOConverter.ofTicketPool(entry.getKey());
      var tdDTOs = new ArrayList<TicketDefinitionDTO>();
      for (AvailableTicketNumberAssociation a : entry.getValue()) {
        var tdDTO = ModelObjectsToDTOConverter.ofTicketDefinition(a.getTicketDefinition());
        tdDTO.availableTicketsNumber = a.getAvailableTicketsNumber();
        tdDTOs.add(tdDTO);
      }
      tpDTO.ticketDefinitions = tdDTOs;
      result.ticketPools.add(tpDTO);
    }

    return result;
  }

  private boolean areDatesEquals(Date date1, Date date2) {
    return date1.equals(date2);
  }

  private boolean checkDates(Date date, TicketPoolDefinition tpd) {
    if (tpd.getIsCyclic()) {
      try {
        var d = tpService.getStartDateForNewInstanceOfCyclicPool(tpd, date);
        return date.equals(d);
      } catch (CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException e) {
        return false;
      }
    }
    return date.equals(tpd.getStartDate());
  }

  private List<AvailableTicketNumberAssociation> getForTicketPool(TicketPool tp) {
    return dao.getForTicketPool(tp);
  }

  private List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    return dao.getForTicketPoolDefinition(tpd);
  }

}
