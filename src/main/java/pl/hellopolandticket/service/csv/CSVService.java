package pl.hellopolandticket.service.csv;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.SightEventService;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.TicketDefinitionService;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketDefinitionCSV;
import pl.hellopolandticket.service.dto.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.event.SightsImportEvent;
import pl.hellopolandticket.service.event.TicketDefinitionsImportEvent;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class CSVService {

  @Inject
  private SightService sightService;

  @Inject
  private SightEventService sightEventService;

  @Inject
  private TicketDefinitionService ticketDefinitionService;

  @Inject
  private Event<SightsImportEvent> sightsImportEvent;

  @Inject
  private Event<TicketDefinitionsImportEvent> ticketDefinitionsImportEvent;

  @Inject
  private ExceptionFactory exceptionFactory;

  @Inject
  private PartnerDao partnerDao;

  public void importSightsFromCSV(List<SightCSV> sightsCSV) {
    try {
      List<Sight> sights = sightsCSV.stream()
          .map(this::createSightOfSightCSV)
          .collect(toList());

      sights.forEach(sight -> sightService.save(sight));

      sightsImportEvent.fireAsync(createSightsImportEvent(sights));
    } catch (Exception e) {
      throw exceptionFactory.importingDataException();
    }
  }

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

  private Sight createSightOfSightCSV(SightCSV sightCSV) {
    SightLocation sightLocation = SightLocation.builder()
        .latitude(sightCSV.getLatitude())
        .longitude(sightCSV.getLongitude())
        .street(sightCSV.getStreet())
        .zipCode(sightCSV.getZipCode())
        .city(sightCSV.getCity())
        .country(sightCSV.getCountry())
        .build();

    return Sight.builder()
        .name(sightCSV.getName())
        .lead(sightCSV.getLead())
        .description(sightCSV.getDescription())
        .mainImageUrl(sightCSV.getMainImageUrl())
        .email(sightCSV.getEmail())
        .phone(sightCSV.getPhone())
        .sightLocation(sightLocation)
        .partner(partnerDao.findByUserEmail(sightCSV.getUserEmail()))
        .build();
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

  private SightsImportEvent createSightsImportEvent(List<Sight> sights) {
    List<pl.hellopoland.dto.Sight> sigtsDTO = sights.stream()
        .map(ModelObjectsToDTOConverter::ofSight)
        .collect(toList());

    return SightsImportEvent.builder()
        .sights(sigtsDTO)
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