package pl.hellopolandticket.service.dto;

import static java.util.stream.Collectors.toList;

import pl.hellopoland.dto.Location;
import pl.hellopoland.dto.Sight;
import pl.hellopoland.dto.Ticket;
import pl.hellopolandticket.model.SightEvent;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.TicketDefinition;

public class ModelObjectsToDTOConverter {

  public static Ticket ofTicketDefinition(TicketDefinition ticketDefinition) {
    Ticket ticketDTO = new Ticket();

    ticketDTO.id = ticketDefinition.getId();
    ticketDTO.name = ticketDefinition.getName();
    ticketDTO.price = ticketDefinition.getPrice();
    ticketDTO.predefinedDate = ticketDefinition.getPredefinedDate();
    ticketDTO.date = ticketDefinition.getDate();

    return ticketDTO;
  }

  public static Location ofSightLocation(SightLocation sightLocation) {
    Location locationDTO = new Location();

    locationDTO.latitude = sightLocation.getLatitude();
    locationDTO.longitude = sightLocation.getLongitude();
    locationDTO.street = sightLocation.getStreet();
    locationDTO.zipCode = sightLocation.getZipCode();
    locationDTO.city = sightLocation.getCity();
    locationDTO.country = sightLocation.getCountry();

    return locationDTO;
  }

  public static Sight ofSightEvent(SightEvent sightEvent) {
    Sight sight = new Sight();

    sight.name = sightEvent.getName();
    sight.description = sightEvent.getDescription();
    sight.email = sightEvent.getEmail();
    sight.phone = sightEvent.getPhone();
    sight.mainImageUrl = sightEvent.getMainImageUrl();

    sight.tickets = sightEvent.getTicketDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketDefinition)
        .collect(toList());

    return sight;
  }
}
