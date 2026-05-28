package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.hellopoland.dto.TicketTypeDTO;
import pl.hellopolandticket.dao.TicketTypeDao;
import pl.hellopolandticket.model.ticket.partner.TicketType;
import pl.hellopolandticket.service.exception.badrequest.BadRequestException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class TicketTypeService extends ServiceSuperclass {

  @Inject
  private TicketTypeDao ticketTypeDao;

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public List<TicketTypeDTO> getActiveList() {
    return ticketTypeDao.getActiveList().stream()
        .map(ModelObjectsToDTOConverter::ofTicketType)
        .collect(Collectors.toList());
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public TicketType getActiveTicketTypeOrThrow(Long ticketTypeId) {
    TicketType ticketType = ticketTypeDao.findById(ticketTypeId).orElse(null);
    if (ticketType == null || !ticketType.isActive()) {
      throw new BadRequestException("Ticket type does not exist or is not active.");
    }
    return ticketType;
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public boolean isNormalTicketType(TicketType ticketType) {
    return ticketType != null && TicketType.NORMALNY_CODE.equals(ticketType.getCode());
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public boolean isPreferredPriceFromNormalTicketType(TicketType ticketType) {
    return isNormalTicketType(ticketType);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_EXTERNAL_USER})
  public boolean isPreferredPriceFromReducedTicketType(TicketType ticketType) {
    return ticketType != null && (TicketType.ULGOWY_CODE.equals(ticketType.getCode())
        || TicketType.ULGOWY_STUDENT_UCZEN_CODE.equals(ticketType.getCode()));
  }

}
