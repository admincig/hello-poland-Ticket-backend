package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private PartnerDao partnerDao;

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

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<TicketDefinitionDTO> getList(CurrentUser currentUser) {
    List<TicketDefinition> bos =
        ticketDefinitionDao.getList(partnerDao.findByUserEmail(currentUser.getPrincipal()));
    return bos.stream().map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(Collectors.toList());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public TicketDefinition get(Long id) {
    return ticketDefinitionDao.findById(id);
  }

}
