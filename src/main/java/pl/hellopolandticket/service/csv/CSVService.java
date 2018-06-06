package pl.hellopolandticket.service.csv;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.SightEventService;
import pl.hellopolandticket.service.TicketDefinitionService;
import pl.hellopolandticket.service.csv.pojo.TicketDefinitionCSV;
import pl.hellopolandticket.service.dto.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.event.TicketDefinitionsImportEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class CSVService {

  @Inject
  private SightEventService sightEventService;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  private Event<TicketDefinitionsImportEvent> ticketDefinitionsImportEvent;

  @Inject
  private ExceptionFactory exceptionFactory;

  public void importTicketDefinitionsFromCSV(List<TicketDefinitionCSV> ticketDefinitionsCSV) {
    try {
      List<TicketDefinition> ticketDefinitions = ticketDefinitionsCSV.stream()
          .map(this::createTicketDefinitionOfTicketDefinitionCSV)
          .collect(toList());

      ticketDefinitions.forEach(ticketDefinition -> ticketDefinitionService.save(ticketDefinition));

      ticketDefinitionsImportEvent.fireAsync(createTicketsImportEvent(ticketDefinitions));
    } catch (Exception e) {
      throw exceptionFactory.importingDataException();
    }
  }


  private TicketDefinition createTicketDefinitionOfTicketDefinitionCSV(
      TicketDefinitionCSV ticketDefinitionCSV) {
    return TicketDefinition.builder()
        .name(ticketDefinitionCSV.getName())
        .price(ticketDefinitionCSV.getPrice())
        .predefinedDate(ticketDefinitionCSV.getPredefinedDate())
        .date(ticketDefinitionCSV.getDate())
        .dateType(ticketDefinitionCSV.getDateType())
        .sightEvent(sightEventService.findSightEventById(ticketDefinitionCSV.getSightEventId()))
        .build();
  }

  private TicketDefinitionsImportEvent createTicketsImportEvent(
      List<TicketDefinition> ticketDefinitions) {
    List<pl.hellopoland.dto.Ticket> ticketsDTO = ticketDefinitions.stream()
        .map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(toList());

    return TicketDefinitionsImportEvent.builder()
        .ticketDefinitions(ticketsDTO)
        .build();
  }
}