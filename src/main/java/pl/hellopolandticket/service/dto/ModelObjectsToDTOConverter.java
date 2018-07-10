package pl.hellopolandticket.service.dto;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import pl.hellopoland.dto.DateType;
import pl.hellopoland.dto.Image;
import pl.hellopoland.dto.Location;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.TicketDefinition;

public class ModelObjectsToDTOConverter {

  public static pl.hellopoland.dto.SightEvent ofSightEvent(SightEvent sightEvent, Long sightId) {
    pl.hellopoland.dto.SightEvent sightEventDTO = new pl.hellopoland.dto.SightEvent();

    sightEventDTO.id = sightEvent.getId();
    sightEventDTO.name = sightEvent.getName();
    sightEventDTO.date = sightEvent.getDate();
    sightEventDTO.description = sightEvent.getDescription();
    sightEventDTO.duration = sightEvent.getDuration();
    sightEventDTO.mainImage = new Image();
    sightEventDTO.mainImage.original = sightEvent.getMainImageUrl();
    sightEventDTO.email = sightEvent.getEmail();
    sightEventDTO.phone = sightEvent.getPhone();
    sightEventDTO.sightId = sightId;

    sightEventDTO.location = ofNullable(sightEvent.getSightLocation())
        .map(ModelObjectsToDTOConverter::ofSightLocation)
        .orElse(null);

    sightEventDTO.tickets = sightEvent.getTicketDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(toList());

    return sightEventDTO;
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
