package pl.hellopolandticket.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  public TicketDefinitionDTO add(TicketDefinitionDTO ticketDefinitionDTO,
      Long ticketPoolDefinitionId, CurrentUser currentUser) {
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());

    List<TicketPoolDefinition> ticketPoolDefinitions =
        Collections.singletonList(ticketPoolDefinitionDao.findById(ticketPoolDefinitionId));

    TicketDefinition ticketDefinition =
        TicketDefinition.builder().name(ticketDefinitionDTO.name).price(ticketDefinitionDTO.price)
            .partner(partner).ticketPoolDefinitions(ticketPoolDefinitions).build();

    ticketDefinitionDao.persist(ticketDefinition);

    ticketPoolDefinitions.forEach(tpd -> {
      tpd.getTicketDefinitions().add(ticketDefinition);
    });

    ticketDefinitionDTO.id = ticketDefinition.getId();
    return ticketDefinitionDTO;
  }

  public List<TicketDefinitionDTO> getList(CurrentUser currentUser) {
    List<TicketDefinition> bos =
        ticketDefinitionDao.getList(partnerDao.findByUserEmail(currentUser.getPrincipal()));
    return bos.stream().map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(Collectors.toList());
  }

  public TicketDefinition get(Long id) {
    return ticketDefinitionDao.findById(id);
  }

}
