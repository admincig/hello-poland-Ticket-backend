package pl.hellopolandticket.service.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.Builder;
import pl.hellopoland.dto.AbstractErrorDTO;
import pl.hellopoland.dto.ApplicationPropertyDTO;
import pl.hellopoland.dto.CollectionWrapperDTO;
import pl.hellopoland.dto.FrequencyDataDTO;
import pl.hellopoland.dto.FrequencyTypeDTO;
import pl.hellopoland.dto.ImageDTO;
import pl.hellopoland.dto.LocationDTO;
import pl.hellopoland.dto.OpeningHoursDTO;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopoland.dto.StatusDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketPoolDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopoland.dto.UserAuthDTO;
import pl.hellopoland.dto.UserDTO;
import pl.hellopoland.dto.booking.BookingDTO;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.config.ApplicationProperty;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.model.sightevent.OpeningHours;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.model.sightevent.SightEventLocation;
import pl.hellopolandticket.model.ticket.market.Booking;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.security.CurrentUser;

public class ModelObjectsToDTOConverter {

  public static SightEventDTO ofSightEvent(SightEvent sightEvent, Long sightId) {
    SightEventDTO sightEventDTO = new SightEventDTO();

    sightEventDTO.id = sightEvent.getId();
    sightEventDTO.name = sightEvent.getName();
    sightEventDTO.description = sightEvent.getDescription();
    sightEventDTO.duration = sightEvent.getDuration();
    sightEventDTO.lead = sightEvent.getLead();
    sightEventDTO.mainImage = new ImageDTO();
    sightEventDTO.mainImage.original = sightEvent.getMainImageUrl();
    sightEventDTO.email = sightEvent.getEmail();
    sightEventDTO.phone = sightEvent.getPhone();
    sightEventDTO.generalAdmission = sightEvent.getGeneralAdmission();
    sightEventDTO.sightId = sightId;
    sightEventDTO.blocked = sightEvent.getBlocked();
    sightEventDTO.published = sightEvent.getPublished();
    sightEventDTO.location = ofNullable(sightEvent.getSightEventLocation())
        .map(ModelObjectsToDTOConverter::ofSightLocation).orElse(null);

    sightEventDTO.ticketPoolDefinitions = sightEvent.getTicketPoolDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketPoolDefinition).collect(toList());

    sightEventDTO.openingHours = ofNullable(sightEvent.getOpeningHours())
        .map(v -> v.stream().map(ModelObjectsToDTOConverter::ofOpeningHours).collect(toList()))
        .orElse(null);

    return sightEventDTO;
  }

  private static OpeningHoursDTO ofOpeningHours(OpeningHours openingHours) {
    OpeningHoursDTO dto = new OpeningHoursDTO();
    dto.closeTime = openingHours.getCloseTime();
    dto.day = openingHours.getDay();
    dto.openTime = openingHours.getOpenTime();
    return dto;
  }

  private static LocationDTO ofSightLocation(SightEventLocation sightEventLocation) {
    LocationDTO location = new LocationDTO();

    location.latitude = sightEventLocation.getLatitude();
    location.longitude = sightEventLocation.getLongitude();
    location.street = sightEventLocation.getStreet();
    location.zipCode = sightEventLocation.getZipCode();
    location.city = sightEventLocation.getCity();
    location.country = sightEventLocation.getCountry();

    return location;
  }

  public static TicketDefinitionDTO ofTicketDefinition(TicketDefinition ticketDefinition) {
    TicketDefinitionDTO ticketDefinitionDTO = new TicketDefinitionDTO();

    ticketDefinitionDTO.id = ticketDefinition.getId();
    ticketDefinitionDTO.name = ticketDefinition.getName();
    ticketDefinitionDTO.price = ticketDefinition.getPrice();

    return ticketDefinitionDTO;
  }

  public static SightEventDTO ofSightEventBasic(SightEvent sightEvent) {
    SightEventDTO sightEventDTO = new SightEventDTO();

    sightEventDTO.id = sightEvent.getId();
    sightEventDTO.name = sightEvent.getName();
    sightEventDTO.blocked = sightEvent.getBlocked();
    sightEventDTO.published = sightEvent.getPublished();

    return sightEventDTO;
  }

  public static TicketDTO ofTicket(Ticket ticket) {
    TicketDTO ticketDTO = new TicketDTO();

    ticketDTO.id = ticket.getId();
    ticketDTO.name = ticket.getName();
    ticketDTO.price = ticket.getPrice();
    ticketDTO.date = (ticket.getDate());
    ticketDTO.status = StatusDTO.valueOf(ticket.getStatus().name());
    ticketDTO.serialNumber = ticket.getSerialNumber();
    ticketDTO.ticketDefinitionId = ticket.getTicketDefinition().getId();
    ticketDTO.tickerPoolDefinitionId = ticket.getTicketPool().getTicketPoolDefinition().getId();
    ticketDTO.bookingId = ticket.getBooking().getId();
    ticketDTO.booking = ofBookingBasic(ticket.getBooking());
    ticketDTO.wholeDay = ticket.getTicketPool().getTicketPoolDefinition().isWholeDay();

    return ticketDTO;
  }

  public static TicketDTO ofTicketWithQrCode(Ticket ticket, ByteArrayOutputStream qrCode) {
    TicketDTO ticketDTO = ofTicket(ticket);
    ticketDTO.qrCode = qrCode;

    return ticketDTO;
  }

  public static BookingDTO ofBooking(Booking booking) {
    BookingDTO bookingDTO = ofBookingBasic(booking);

    bookingDTO.status = StatusDTO.valueOf(booking.getStatus().name());
    bookingDTO.serialNumber = booking.getSerialNumber();
    bookingDTO.tickets =
        booking.getTickets().stream().map(ModelObjectsToDTOConverter::ofTicket).collect(toList());

    return bookingDTO;
  }

  public static BookingDTO ofBookingBasic(Booking booking) {
    BookingDTO bookingDTO = new BookingDTO();

    bookingDTO.id = booking.getId();
    bookingDTO.date = booking.getDate();
    bookingDTO.customerName = booking.getCustomerName();
    bookingDTO.customerEmail = booking.getCustomerEmail();

    return bookingDTO;
  }

  public static PartnerDTO ofPartnerWithToken(Partner partner, String token) {
    PartnerDTO partnerDTO = ofPartner(partner);
    partnerDTO.token = token;

    return partnerDTO;
  }

  public static PartnerDTO ofPartner(Partner partner) {
    PartnerDTO partnerDTO = new PartnerDTO();

    partnerDTO.id = partner.getId();
    partnerDTO.name = partner.getName();
    partnerDTO.users = Optional.ofNullable(partner.getUsers()).orElse(Collections.emptyList())
        .stream().map(ModelObjectsToDTOConverter::ofUser).collect(toList());
    partnerDTO.sightEvents =
        Optional.ofNullable(partner.getSightEvents()).orElse(Collections.emptyList()).stream()
            .map(ModelObjectsToDTOConverter::ofSightEventBasic).collect(toList());

    return partnerDTO;
  }

  public static UserDTO ofUser(User user) {
    UserDTO userDTO = new UserDTO();

    userDTO.id = user.getId();
    userDTO.name = user.getName();
    userDTO.email = user.getEmail();

    return userDTO;
  }

  public static ApplicationPropertyDTO ofApplicationProperty(
      ApplicationProperty applicationProperty) {

    ApplicationPropertyDTO applicationPropertyDTO = new ApplicationPropertyDTO();

    applicationPropertyDTO.propertyName = applicationProperty.getPropertyName();
    applicationPropertyDTO.propertyValue = applicationProperty.getPropertyValue();

    return applicationPropertyDTO;
  }

  public static CollectionWrapperDTO ofCollection(Collection<?> collection) {
    CollectionWrapperDTO collectionWrapperDTO = new CollectionWrapperDTO();

    collectionWrapperDTO.items = collection;

    return collectionWrapperDTO;
  }

  @Builder(builderMethodName = "abstractErrorDTOBuilder")
  public static AbstractErrorDTO abstractErrorDTO(Class<? extends Exception> exception,
      String message, Object object) {
    AbstractErrorDTO abstractErrorDTO = new AbstractErrorDTO();

    abstractErrorDTO.exception = exception.getSimpleName();
    abstractErrorDTO.message = message;
    abstractErrorDTO.object = object;

    return abstractErrorDTO;
  }

  public static UserAuthDTO ofCurrentUser(CurrentUser currentUser) {
    UserAuthDTO userAuthDTO = new UserAuthDTO();

    userAuthDTO.login = currentUser.getPrincipal();
    userAuthDTO.accessToken = currentUser.getAccessToken();
    userAuthDTO.refreshToken = currentUser.getRefreshToken();

    return userAuthDTO;
  }


  public static TicketPoolDTO ofTicketPool(TicketPool ticketPool) {
    TicketPoolDTO ticketPoolDTO = new TicketPoolDTO();

    ticketPoolDTO.id = ticketPool.getId();
    ticketPoolDTO.name = ticketPool.getName();
    ticketPoolDTO.availableTicketsNumber = ticketPool.getAvailableTicketsNumber();
    ticketPoolDTO.startDate = ticketPool.getStartDate();
    ticketPoolDTO.endDate = ticketPool.getEndDate();
    ticketPoolDTO.entryStartDate = ticketPool.getEntryStartDate();
    ticketPoolDTO.entryEndDate = ticketPool.getEntryEndDate();
    ticketPoolDTO.ticketPoolDefinitionId =
        ofNullable(ticketPool.getTicketPoolDefinition()).map(tpd -> tpd.getId()).orElse(null);

    return ticketPoolDTO;
  }

  public static TicketPoolDefinitionDTO ofTicketPoolDefinitionBasic(
      TicketPoolDefinition ticketPoolDefinition) {
    TicketPoolDefinitionDTO ticketPoolDefinitionDTO = new TicketPoolDefinitionDTO();
    ticketPoolDefinitionDTO.id = ticketPoolDefinition.getId();
    ticketPoolDefinitionDTO.name = ticketPoolDefinition.getName();
    ticketPoolDefinitionDTO.availableTicketsNumber =
        ticketPoolDefinition.getAvailableTicketsNumber();
    ticketPoolDefinitionDTO.isCyclic = ticketPoolDefinition.getIsCyclic();
    ticketPoolDefinitionDTO.frequencyData =
        ofFrequencyData(ticketPoolDefinition.getFrequencyData());
    ticketPoolDefinitionDTO.startDate = ticketPoolDefinition.getStartDate();
    ticketPoolDefinitionDTO.endDate = ticketPoolDefinition.getEndDate();
    ticketPoolDefinitionDTO.entryStartDate = ticketPoolDefinition.getEntryStartDate();
    ticketPoolDefinitionDTO.entryEndDate = ticketPoolDefinition.getEntryEndDate();
    ticketPoolDefinitionDTO.sightEventId = ticketPoolDefinition.getSightEvent().getId();
    ticketPoolDefinitionDTO.deleted = ticketPoolDefinition.isDeleted();
    ticketPoolDefinitionDTO.wholeDay = ticketPoolDefinition.isWholeDay();
    return ticketPoolDefinitionDTO;
  }

  public static TicketPoolDefinitionDTO ofTicketPoolDefinition(
      TicketPoolDefinition ticketPoolDefinition) {
    TicketPoolDefinitionDTO ticketPoolDefinitionDTO =
        ofTicketPoolDefinitionBasic(ticketPoolDefinition);
    ticketPoolDefinitionDTO.ticketDefinitions = ticketPoolDefinition.getTicketDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketDefinition).collect(toList());
    return ticketPoolDefinitionDTO;
  }

  private static FrequencyDataDTO ofFrequencyData(FrequencyData frequencyData) {
    if (frequencyData != null && frequencyData.getFrequencyType() != null) {
      FrequencyDataDTO frequencyDataDTO = new FrequencyDataDTO();
      frequencyDataDTO.frequencyType =
          FrequencyTypeDTO.valueOf(frequencyData.getFrequencyType().name());
      if (frequencyData.getDaysOfMonth() != null) {
        // fetch first
        frequencyData.getDaysOfMonth().size();
      }
      frequencyDataDTO.daysOfMonth = frequencyData.getDaysOfMonth();
      if (frequencyData.getDaysOfWeek() != null) {
        // fetch first
        frequencyData.getDaysOfWeek().size();
      }
      frequencyDataDTO.daysOfWeek = frequencyData.getDaysOfWeek();
      frequencyDataDTO.frequency = frequencyData.getFrequency();
      frequencyDataDTO.endDate = frequencyData.getEndDate();
      frequencyDataDTO.startDate = frequencyData.getStartDate();
      return frequencyDataDTO;
    }
    return null;
  }

}
