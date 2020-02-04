package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.AvailableTicketNumberAssociationDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.AvailableTicketNumberAssociationDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.exception.notfound.NonRollbackResourceNotFoundException;
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
  private TicketDefinitionDao tdDao;

  @Inject
  private TicketPoolService tpService;

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public void add(TicketPoolDefinition ticketPoolDefinition,
      List<TicketDefinitionDTO> tds) {
    if (tds != null && !tds.isEmpty()) {
      for (var td : tds) {
        var bo = AvailableTicketNumberAssociation.builder()
            .ticketDefinition(tdDao.findById(td.id))
            .ticketPoolDefinition(ticketPoolDefinition)
            .availableTicketsNumber(
                td.availableTicketsNumber == null
                    ? ticketPoolDefinition.getAvailableTicketsNumber()
                    : td.availableTicketsNumber)
            .build();
        dao.persist(bo);
      }
    }
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public void createChildrenAtnas(TicketPoolDefinition tpd, TicketPool pool) {
    var tds = tpd.getTicketDefinitions();
    if (tds != null && !tds.isEmpty()) {
      List<TicketDefinition> notDeleted =
          tds.stream().filter(td -> !td.isDeleted()).collect(Collectors.toList());
      if (!notDeleted.isEmpty()) {
        for (TicketDefinition td : notDeleted) {
          createAtna(tpd, pool, td);
        }
      }
    }
  }

  private void createAtna(TicketPoolDefinition tpd, TicketPool pool, TicketDefinition td) {
    AvailableTicketNumberAssociation association = null;
    try {
      association = dao.findForTicketPoolDefinitionAndTicketDefinition(tpd, td);
    } catch (NonRollbackResourceNotFoundException e) {
      logger.log(Level.DEBUG, "atna for td probably deleted");
      return;
    }
    var bo = AvailableTicketNumberAssociation.builder()
        .ticketDefinition(td)
        .ticketPool(pool)
        .availableTicketsNumber(association.getAvailableTicketsNumber())
        .parent(association)
        .discount(association.getDiscount())
        .build();
    dao.persist(bo);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<AvailableTicketNumberAssociation> getAvailabilityOfTicketsForNonCyclicTicketPool(
      TicketPool tp) {
    return dao.getNotZeroForTicketPool(tp);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public AvailableTicketNumberAssociationDTO checkAvailabilityOfTickets(Long sightEventId,
      Date fromDate, Date toDate) {
    var se = seService.findSightEventById(sightEventId);
    se.getTicketPoolDefinitions().size();

    var associationsTPD = new HashSet<AvailableTicketNumberAssociation>();
    var associationsTP = new HashSet<AvailableTicketNumberAssociation>();

    LocalDate fromDateLD =
        fromDate != null ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            : LocalDate.now();

    if (toDate == null) {
      fillTicketAssociations(se, associationsTPD, associationsTP, fromDateLD);
    } else {
      LocalDate toDateLD =
          toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1);
      if (toDateLD.isBefore(fromDateLD)) {
        throw new ConflictingException(
            "toDate[" + toDate + "] is before fromDate[" + fromDate + "]");
      }
      fromDateLD.datesUntil(toDateLD).forEach(ld -> {
        fillTicketAssociations(se, associationsTPD, associationsTP, ld);
      });
    }

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
        if (!a.isDeleted() && !a.getTicketDefinition().isDeleted()) {
          a.getTicketDefinition().setInterestingAtna(a);
          var tdDTO = ModelObjectsToDTOConverter.ofTicketDefinition(a.getTicketDefinition());
          if (tpdDTO.availableTicketsNumber < 0) {
            tdDTO.availableTicketsNumber = a.getAvailableTicketsNumber();
          } else {
            tdDTO.availableTicketsNumber = -1;
          }
          tdDTOs.add(tdDTO);
        }
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
        if (!a.isDeleted() && !a.getTicketDefinition().isDeleted()) {
          a.getTicketDefinition().setInterestingAtna(a.getParent());
          var tdDTO = ModelObjectsToDTOConverter.ofTicketDefinition(a.getTicketDefinition());
          if (tpDTO.availableTicketsNumber < 0) {
            tdDTO.availableTicketsNumber = a.getAvailableTicketsNumber();
          } else {
            tdDTO.availableTicketsNumber = -1;
          }
          tdDTOs.add(tdDTO);
        }
      }
      tpDTO.ticketDefinitions = tdDTOs;
      result.ticketPools.add(tpDTO);
    }

    return result;
  }

  private void fillTicketAssociations(SightEvent se,
      HashSet<AvailableTicketNumberAssociation> associationsTPD,
      HashSet<AvailableTicketNumberAssociation> associationsTP, LocalDate localDate) {
    se.getTicketPoolDefinitions().stream()
        .filter(tpd -> checkDates(localDate, tpd) && !tpd.isDeleted())
        .forEach(tpd -> {
          List<TicketPool> ticketPools = tpd.getTicketPools();
          tpd.getTicketPools().size();
          List<TicketPool> tps = getFilteredTpsByDates(localDate, ticketPools);
          if (tps != null && !tps.isEmpty()) {
            fillFromTPs(associationsTP, tps);
          } else {
            fillFromTPD(associationsTPD, tpd);
          }
        });
  }

  private List<TicketPool> getFilteredTpsByDates(LocalDate localDate,
      List<TicketPool> ticketPools) {
    return ticketPools.stream()
        .filter(p -> localDate
            .isEqual(p.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()))
        .collect(Collectors.toList());
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
    return localDate
        .isEqual(tpd.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<AvailableTicketNumberAssociation> getForTicketPool(TicketPool tp) {
    return dao.getUndeletedForTicketPool(tp);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<AvailableTicketNumberAssociation> getForTicketPoolDefinition(
      TicketPoolDefinition tpd) {
    return dao.getUndeletedForTicketPoolDefinition(tpd);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public AvailableTicketNumberAssociation update(AvailableTicketNumberAssociation bo) {
    return dao.update(bo);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public AvailableTicketNumberAssociation add(AvailableTicketNumberAssociation newAtna) {
    newAtna = dao.persist(newAtna);
    TicketDefinition td = newAtna.getTicketDefinition();
    td = tdDao.findById(td.getId());
    td.getAtnas().add(newAtna);
    if (newAtna.getTicketPoolDefinition() != null) {
      newAtna.getTicketPoolDefinition().getAtnas().add(newAtna);
    }
    return newAtna;
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public AvailableTicketNumberAssociation get(Long id) {
    return em.find(AvailableTicketNumberAssociation.class, id);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public TicketDefinition getTD(Long id) {
    return em.find(TicketDefinition.class, id);
  }

}
