package pl.hellopolandticket.service.dto;

import pl.hellopoland.dto.Location;
import pl.hellopoland.dto.Sight;
import pl.hellopoland.dto.Ticket;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.TicketDefinition;

public class ModelObjectsToDTOConverter {

  public static Ticket ofTicket(TicketDefinition ticketDefinition) {
    Ticket ticketDTO = new Ticket();

    ticketDTO.id = ticketDefinition.getId();
    ticketDTO.name = ticketDefinition.getName();
    ticketDTO.price = ticketDefinition.getPrice();
    ticketDTO.predefinedDate = ticketDefinition.getPredefinedDate();
    ticketDTO.date = ticketDefinition.getDate();

    return ticketDTO;
  }

  public static Sight ofSight(pl.hellopolandticket.model.Sight sight) {
    Sight sightDTO = new Sight();

    sightDTO.name = sight.getName();
    sightDTO.lead = sight.getLead();
    sightDTO.description = sight.getDescription();
    sightDTO.mainImageUrl = sight.getMainImageUrl();
    sightDTO.email = sight.getEmail();
    sightDTO.phone = sight.getPhone();
    sightDTO.location = ofSightLocation(sight.getSightLocation());

    return sightDTO;
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
}
