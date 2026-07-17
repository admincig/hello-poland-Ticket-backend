package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolType;
import pl.hellopolandticket.model.ticket.partner.TicketType;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.util.TicketPoolDefinitionAtnasComparerResult;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private AvailableTicketNumberAssociationService atnaService;

  @Inject
  private TicketTypeService ticketTypeService;

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketDefinitionDTO add(TicketDefinitionDTO ticketDefinitionDTO, CurrentUser currentUser) {
    if (ticketDefinitionDTO.price < 0) {
      logger.log(Level.ERROR, "Ticket definition [id=" + ticketDefinitionDTO.id
          + "]. The ticket price must be greater than 0");
      throw new BadRequestException("The ticket price must be greater than 0");
    }
    if (ticketDefinitionDTO.ticketTypeId == null) {
      throw new BadRequestException("Ticket type is required.");
    }
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
    TicketType ticketType = ticketTypeService.getActiveTicketTypeOrThrow(
        ticketDefinitionDTO.ticketTypeId);

    TicketDefinition ticketDefinition = TicketDefinition.builder().name(ticketDefinitionDTO.name)
        .price(ticketDefinitionDTO.price).ticketType(ticketType).partner(partner).build();
    ticketDefinitionDao.persist(ticketDefinition);

    ticketDefinitionDTO.id = ticketDefinition.getId();
    ticketDefinitionDTO.ticketType = ModelObjectsToDTOConverter.ofTicketType(ticketType);
    return ticketDefinitionDTO;
  }

    @PermitAll
    @Transactional
  public List<TicketDefinitionDTO> getList(List<Long> atnaIds, CurrentUser currentUser) {
    List<TicketDefinition> bos = null;
    if (currentUser == null || currentUser.hasRole(Role.ROLE_ADMIN)) {
      bos = ticketDefinitionDao.getUndeletedList(atnaIds);
    } else {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      bos = ticketDefinitionDao.getUndeletedList(partner);
    }
      return bos.stream()
              .map(td -> {
                  td.setInterestingAtna(
                          td.getAtnasConnectedToPoolDefinitions().stream()
                                  .filter(a -> !a.isDeleted())
                                  .findFirst()
                                  .orElse(null)
                  );
                  return ModelObjectsToDTOConverter.ofTicketDefinition(td);
              })
              .collect(Collectors.toList());

  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public void delete(Long id, CurrentUser currentUser) {
    TicketDefinition td = ticketDefinitionDao.findById(id);
    if (!currentUser.hasRole(Role.ROLE_ADMIN)) {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      if (!td.getPartner().getId().equals(partner.getId())) {
        throw new ForbiddenException();
      }
    }
    validateNormalTicketRemoval(td);
    td.setDeleted(true);
    TicketPoolDefinitionAtnasComparerResult diffs = new TicketPoolDefinitionAtnasComparerResult();
    diffs.toRemove.addAll(td.getAtnasConnectedToPoolDefinitions());
    new TicketPoolDefinitionAtnasDiffApplier(atnaService).apply(diffs);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<TicketDefinitionDTO> update(TicketDefinitionDTO dto, CurrentUser currentUser) {
    if (dto.price < 0) {
      logger.log(Level.ERROR, "Ticket definition [id=" + dto.id
          + "]. The ticket price must be greater than 0");
      throw new BadRequestException("The ticket price must be greater than 0");
    }
    if (dto.ticketTypeId == null) {
      throw new BadRequestException("Ticket type is required.");
    }
    TicketDefinition td = ticketDefinitionDao.findById(dto.id);
    if (!currentUser.hasRole(Role.ROLE_ADMIN)) {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      if (!td.getPartner().getId().equals(partner.getId())) {
        throw new ForbiddenException();
      }
    }
    TicketType newTicketType = ticketTypeService.getActiveTicketTypeOrThrow(dto.ticketTypeId);
    if (ticketTypeService.isNormalTicketType(td.getTicketType())
        && !ticketTypeService.isNormalTicketType(newTicketType)) {
      validateNormalTicketRemoval(td);
    }
    validateTicketTypeChangeForPoolTypes(td, newTicketType);
    td.setName(dto.name);
    td.setPrice(dto.price);
    td.setTicketType(newTicketType);
    return getList(null, currentUser);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public TicketDefinitionDTO get(Long id, CurrentUser currentUser) {
    TicketDefinition td = ticketDefinitionDao.findById(id);
    if (!currentUser.hasRole(Role.ROLE_ADMIN)) {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      if (!td.getPartner().getId().equals(partner.getId())) {
        throw new ForbiddenException();
      }
    }
    return ModelObjectsToDTOConverter.ofTicketDefinition(td);
  }

  @PermitAll
  public List<TicketDefinitionDTO> getListForMarket(List<Long> atnaIds) {
        return ticketDefinitionDao.getUndeletedList(atnaIds).stream()
                .map(ModelObjectsToDTOConverter::ofTicketDefinition)
                .collect(Collectors.toList());
  }

  private void validateNormalTicketRemoval(TicketDefinition ticketDefinition) {
    if (!ticketTypeService.isNormalTicketType(ticketDefinition.getTicketType())) {
      return;
    }

    List<TicketPoolDefinition> affectedPoolDefinitions = ticketDefinition
        .getAtnasConnectedToPoolDefinitions().stream()
        .filter(atna -> !atna.isDeleted())
        .map(AvailableTicketNumberAssociation::getTicketPoolDefinition)
        .filter(Objects::nonNull)
        .filter(tpd -> !tpd.isDeleted() && Boolean.TRUE.equals(tpd.getSightEvent().getActive()))
        .distinct()
        .collect(Collectors.toList());

    for (TicketPoolDefinition tpd : affectedPoolDefinitions) {
      boolean hasAnotherNormalTicket = tpd.getUndeletedAtnas().stream()
          .map(AvailableTicketNumberAssociation::getTicketDefinition)
          .filter(td -> !td.isDeleted())
          .anyMatch(td -> !Objects.equals(td.getId(), ticketDefinition.getId())
              && ticketTypeService.isNormalTicketType(td.getTicketType()));
      if (!hasAnotherNormalTicket) {
        throw new ConflictingException(
            "Nie można usunąć lub zmienić typu ostatniego biletu Normalny przypisanego do oferty.");
      }
    }
  }

  private void validateTicketTypeChangeForPoolTypes(TicketDefinition ticketDefinition,
      TicketType newTicketType) {
    List<TicketPoolDefinition> affectedPoolDefinitions = ticketDefinition
        .getAtnasConnectedToPoolDefinitions().stream()
        .filter(atna -> !atna.isDeleted())
        .map(AvailableTicketNumberAssociation::getTicketPoolDefinition)
        .filter(Objects::nonNull)
        .filter(tpd -> !tpd.isDeleted())
        .distinct()
        .collect(Collectors.toList());

    boolean isSpecialTicket = ticketTypeService.isSpecialTicketType(newTicketType);
    for (TicketPoolDefinition tpd : affectedPoolDefinitions) {
      boolean isPromotionalPool = TicketPoolType.PROMOTIONAL.equals(tpd.getPoolType());
      if (isPromotionalPool && !isSpecialTicket) {
        throw new ConflictingException(
            "Ticket assigned to promotional pool must be special.");
      }
      if (!isPromotionalPool && isSpecialTicket) {
        throw new ConflictingException(
            "Special ticket can be assigned only to promotional pool.");
      }
    }
  }
}
