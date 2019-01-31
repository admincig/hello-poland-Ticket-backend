package pl.hellopolandticket.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
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

    se.getTicketPoolDefinitions().stream().filter(tpd -> checkDates(date, tpd) && !tpd.isDeleted())
        .forEach(tpd -> {
          List<TicketPool> ticketPools = tpd.getTicketPools();
          tpd.getTicketPools().size();
          if (!tpd.getIsCyclic() && ticketPools != null && !ticketPools.isEmpty()) {
            var tps = getFilteredTpsByDates(date, ticketPools);
            if (!tps.isEmpty()) {
              fillFromTPs(associationsTP, tps);
            }
          }
          if (ticketPools == null || ticketPools.isEmpty()) {
            fillFromTPD(associationsTPD, tpd);
          }
          if (tpd.getIsCyclic() && ticketPools != null && !ticketPools.isEmpty()) {
            var tps = getFilteredTpsByDates(date, ticketPools);
            if (!tps.isEmpty()) {
              fillFromTPs(associationsTP, tps);
            } else {
              fillFromTPD(associationsTPD, tpd);
            }
          }
        });

    var result = new AvailableTicketNumberAssociationDTO();
    result.ticketPoolDefinitions = new ArrayList<>();
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
      result.ticketPoolDefinitions.add(tpdDTO);
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

  public AvailableTicketNumberAssociationDTO checkAvailabilityOfTickets(Long sightEventId,
      Date fromDate, Date toDate) {
    var se = seService.findSightEventById(sightEventId);
    se.getTicketPoolDefinitions().size();

    var associationsTPD = new HashSet<AvailableTicketNumberAssociation>();
    var associationsTP = new HashSet<AvailableTicketNumberAssociation>();

    var fromDateLD = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    var toDateLD =
        toDate != null ? toDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toLocalDate()
            : fromDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1).toLocalDate();

    fromDateLD.datesUntil(toDateLD).forEach(ld -> {
      ZonedDateTime dateLdt = ld.atStartOfDay(ZoneId.systemDefault());
      Date date = Date.from(dateLdt.toInstant());
      se.getTicketPoolDefinitions().stream().filter(tpd -> checkDates(ld, tpd) && !tpd.isDeleted())
          .forEach(tpd -> {
            List<TicketPool> ticketPools = tpd.getTicketPools();
            tpd.getTicketPools().size();
            if (!tpd.getIsCyclic() && ticketPools != null && !ticketPools.isEmpty()) {
              var tps = getFilteredTpsByDates(date, ticketPools);
              if (!tps.isEmpty()) {
                fillFromTPs(associationsTP, tps);
              }
            }
            if (ticketPools == null || ticketPools.isEmpty()) {
              fillFromTPD(associationsTPD, tpd);
            }
            if (tpd.getIsCyclic() && ticketPools != null && !ticketPools.isEmpty()) {
              var tps = getFilteredTpsByDates(date, ticketPools);
              if (!tps.isEmpty()) {
                fillFromTPs(associationsTP, tps);
              } else {
                fillFromTPD(associationsTPD, tpd);
              }
            }
          });
    });
    var result = new AvailableTicketNumberAssociationDTO();
    result.ticketPoolDefinitions = new ArrayList<>();
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
      result.ticketPoolDefinitions.add(tpdDTO);
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

  private List<TicketPool> getFilteredTpsByDates(Date date, List<TicketPool> ticketPools) {
    return ticketPools.stream().filter(p -> areDatesEquals(p.getStartDate(), date))
        .collect(Collectors.toList());
  }

  private void fillFromTPs(ArrayList<AvailableTicketNumberAssociation> associationsTP,
      List<TicketPool> tps) {
    var availableTicketNumbers = new ArrayList<AvailableTicketNumberAssociation>();
    tps.forEach(tp -> availableTicketNumbers.addAll(getForTicketPool(tp)));
    associationsTP.addAll(availableTicketNumbers);
  }

  private void fillFromTPD(ArrayList<AvailableTicketNumberAssociation> associationsTPD,
      TicketPoolDefinition tpd) {
    associationsTPD.addAll(getForTicketPoolDefinition(tpd));
  }

  private void fillFromTPs(HashSet<AvailableTicketNumberAssociation> associationsTP,
      List<TicketPool> tps) {
    var availableTicketNumbers = new ArrayList<AvailableTicketNumberAssociation>();
    tps.forEach(tp -> availableTicketNumbers.addAll(getForTicketPool(tp)));
    associationsTP.addAll(availableTicketNumbers);
  }

  private void fillFromTPD(HashSet<AvailableTicketNumberAssociation> associationsTPD,
      TicketPoolDefinition tpd) {
    associationsTPD.addAll(getForTicketPoolDefinition(tpd));
  }

  private boolean checkDates(Date date, TicketPoolDefinition tpd) {
    if (tpd.getIsCyclic()) {
      try {
        LocalDate dateLd = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime dateLdt = dateLd
            .atTime(tpd.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
        date = Date.from(dateLdt.atZone(ZoneId.systemDefault()).toInstant());
        Date d = tpService.getStartDateForNewInstanceOfCyclicPool(tpd, date);
        return date.equals(d);
      } catch (CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException e) {
        return false;
      }
    }
    return areDatesEquals(date, tpd.getStartDate());
  }

  private boolean checkDates(LocalDate localDate, TicketPoolDefinition tpd) {
    if (tpd.getIsCyclic()) {
      try {
        LocalDateTime dateLdt = localDate
            .atTime(tpd.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
        Date date = Date.from(dateLdt.atZone(ZoneId.systemDefault()).toInstant());
        return date.equals(tpService.getStartDateForNewInstanceOfCyclicPool(tpd, date));
      } catch (CannotCreateTicketPoolForNotCyclicalPoolDefinitionNonRollbackException e) {
        return false;
      }
    }
    return areDatesEquals(localDate,
        tpd.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
  }

  private boolean areDatesEquals(Date date1, Date date2) {
    return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        .isEqual(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
  }

  private boolean areDatesEquals(LocalDate date1, LocalDate date2) {
    return date1.isEqual(date2);
  }

  private List<AvailableTicketNumberAssociation> getForTicketPool(TicketPool tp) {
    return dao.getForTicketPool(tp);
  }

  public List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    return dao.getForTicketPoolDefinition(tpd);
  }

}
