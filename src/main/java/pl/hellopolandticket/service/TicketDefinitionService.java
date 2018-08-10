package pl.hellopolandticket.service;

import static java.util.Optional.ofNullable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.dao.TicketPoolDao;
import pl.hellopolandticket.dao.TicketPoolDefinitionDao;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.validator.TicketPoolValidator;

@Stateless
@LocalBean
public class TicketDefinitionService extends ServiceSuperclass {

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private TicketPoolDefinitionDao ticketPoolDefinitionDao;

  @Inject
  private TicketPoolDao ticketPoolDao;

  @Inject
  private TicketPoolService ticketPoolService;

  @Inject
  private TicketPoolValidator ticketPoolValidator;

  public TicketDefinitionDTO add(TicketDefinitionDTO ticketDefinitionDTO, CurrentUser currentUser) {
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());

    TicketPool ticketPool = ofNullable(ticketDefinitionDTO.ticketPoolId)
        .map(ticketPoolId -> ticketPoolDao.findById(ticketPoolId)).orElse(null);

    List<TicketPoolDefinition> ticketPoolDefinitions =
        ticketPoolDefinitionDao.findByIdsIn(ticketDefinitionDTO.ticketPoolDefinitionIds);

    TicketDefinition ticketDefinition =
        TicketDefinition.builder().name(ticketDefinitionDTO.name).price(ticketDefinitionDTO.price)
            .partner(partner).availableTicketsNumber(ticketDefinitionDTO.availableTicketsNumber)
            .ticketPool(ticketPool).ticketPoolDefinitions(ticketPoolDefinitions).build();

    ticketDefinitionDao.persist(ticketDefinition);

    ticketPoolDefinitions.forEach(tpd -> {
      tpd.getTicketDefinitions().add(ticketDefinition);
    });

    ticketDefinitionDTO.id = ticketDefinition.getId();
    return ticketDefinitionDTO;
  }

  public synchronized TicketDefinition findTicketDefinitionWithTicketPool(Long ticketDefinitionId,
      Long ticketPoolDefinitionId, Date requestedDate) {
    TicketDefinition ticketDefinitionWithTicketPool;

    TicketDefinition ticketDefinition = ticketDefinitionDao.findById(ticketDefinitionId);
    if (ticketDefinition.getTicketDefinitionInstances() != null
        && !ticketDefinition.getTicketDefinitionInstances().isEmpty()) {
      ticketDefinition = ticketDefinition.getTicketDefinitionInstances().stream()
          .max((t1, t2) -> t1.getId().compareTo(t2.getId())).get();
    }

    if (hasTicketPool(ticketDefinition)) {
      ticketPoolValidator.validateRequestedDateEqualsStartDate(
          ticketDefinition.getTicketPool().getStartDate(), requestedDate);

      ticketDefinitionWithTicketPool = ticketDefinition;
    } else {
      TicketPoolDefinition ticketPoolDefinition =
          ticketPoolDefinitionDao.findById(ticketPoolDefinitionId);

      ticketDefinitionWithTicketPool =
          findInTicketDefinitionInstances(ticketDefinition, requestedDate);
      if (ticketDefinitionWithTicketPool == null) {
        TicketPool ticketPool =
            ticketPoolService.createTicketPoolInstance(ticketPoolDefinition, requestedDate);

        return ticketPool.getTicketDefinitions().stream()
            .filter(td -> td.getOriginalTicketDefinition().getId().equals(ticketDefinitionId))
            .findFirst().orElse(null);
      }
    }

    return ticketDefinitionWithTicketPool;
  }

  public boolean hasTicketPool(TicketDefinition ticketDefinition) {
    return ticketDefinition.getTicketPool() != null;
  }

  private TicketDefinition findInTicketDefinitionInstances(TicketDefinition ticketDefinition,
      Date requestedDate) {
    return ticketDefinition.getTicketDefinitionInstances().stream()
        .filter(td -> td.getTicketPool().getDate().equals(requestedDate)
            && td.getName().equals(ticketDefinition.getName()))
        .findFirst().orElse(null);
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
