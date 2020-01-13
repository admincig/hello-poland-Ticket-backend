package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
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

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketDefinitionDTO add(TicketDefinitionDTO ticketDefinitionDTO, CurrentUser currentUser) {
    if (ticketDefinitionDTO.price < 0) {
      logger.log(Level.ERROR, "Ticket definition [id=" + ticketDefinitionDTO.id
          + "]. The ticket price must be greater than 0");
      throw new BadRequestException("The ticket price must be greater than 0");
    }
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());

    TicketDefinition ticketDefinition = TicketDefinition.builder().name(ticketDefinitionDTO.name)
        .price(ticketDefinitionDTO.price).partner(partner).build();
    ticketDefinitionDao.persist(ticketDefinition);

    ticketDefinitionDTO.id = ticketDefinition.getId();
    return ticketDefinitionDTO;
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<TicketDefinitionDTO> getList(CurrentUser currentUser) {
    List<TicketDefinition> bos = null;
    if (currentUser.hasRole(Role.ROLE_ADMIN)) {
      bos = ticketDefinitionDao.getUndeletedList();
    } else {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      bos = ticketDefinitionDao.getUndeletedList(partner);
    }
    return bos.stream().map(ModelObjectsToDTOConverter::ofTicketDefinition)
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
    TicketDefinition td = ticketDefinitionDao.findById(dto.id);
    if (!currentUser.hasRole(Role.ROLE_ADMIN)) {
      Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
      if (!td.getPartner().getId().equals(partner.getId())) {
        throw new ForbiddenException();
      }
    }
    td.setName(dto.name);
    td.setPrice(dto.price);
    return getList(currentUser);
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

}
