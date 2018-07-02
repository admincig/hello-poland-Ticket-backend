package pl.hellopolandticket.service.dto;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import pl.hellopoland.dto.DateType;
import pl.hellopoland.dto.Location;
import pl.hellopoland.dto.SightEventDefinition;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.TicketDefinition;

public class ModelObjectsToDTOConverter {

  public static SightEventDefinition ofSightEvent(SightEvent sightEvent, Long sightId) {
    SightEventDefinition sightEventDefinition = new SightEventDefinition();

    sightEventDefinition.id = sightEvent.getId();
    sightEventDefinition.name = sightEvent.getName();
    sightEventDefinition.date = sightEvent.getDate();
    sightEventDefinition.availableTicketsNumber = sightEvent.getAvailableTicketsNumber();
    sightEventDefinition.description = sightEvent.getDescription();
    sightEventDefinition.duration = sightEvent.getDuration();
    sightEventDefinition.mainImageUrl = sightEvent.getMainImageUrl();
    sightEventDefinition.email = sightEvent.getEmail();
    sightEventDefinition.phone = sightEvent.getPhone();
    sightEventDefinition.sightId = sightId;

    sightEventDefinition.location = ofNullable(sightEvent.getSightLocation())
        .map(ModelObjectsToDTOConverter::ofSightLocation)
        .orElse(null);

    sightEventDefinition.tickets = sightEvent.getTicketDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(toList());

    return sightEventDefinition;
  }

  private static Location ofSightLocation(SightLocation sightLocation) {
    Location location = new Location();

    location.latitude = sightLocation.getLatitude();
    location.longitude = sightLocation.getLongitude();
    location.street = sightLocation.getStreet();
    location.zipCode = sightLocation.getZipCode();
    location.city = sightLocation.getCity();
    location.country = sightLocation.getCountry();

    return location;
  }

  private static pl.hellopoland.dto.TicketDefinition ofTicketDefinition(
      TicketDefinition ticketDefinition) {
    pl.hellopoland.dto.TicketDefinition ticketDefinitionDTO = new pl.hellopoland.dto.TicketDefinition();

    ticketDefinitionDTO.id = ticketDefinition.getId();
    ticketDefinitionDTO.name = ticketDefinition.getName();
    ticketDefinitionDTO.price = ticketDefinition.getPrice();
    ticketDefinitionDTO.predefinedDate = ticketDefinition.getPredefinedDate();
    ticketDefinitionDTO.date = ticketDefinition.getDate();
    ticketDefinitionDTO.dateType = DateType.valueOf(ticketDefinition.getDateType().name());
    ticketDefinitionDTO.sightEventId = ticketDefinition.getSightEvent().getId();

    return ticketDefinitionDTO;
  }
}
