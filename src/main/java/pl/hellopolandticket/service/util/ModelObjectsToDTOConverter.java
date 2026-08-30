package pl.hellopolandticket.service.util;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.Builder;
import pl.hellopoland.dto.AbstractErrorDTO;
import pl.hellopoland.dto.ApplicationPropertyDTO;
import pl.hellopoland.dto.CollectionWrapperDTO;
import pl.hellopoland.dto.DiscountDTO;
import pl.hellopoland.dto.DiscountTypeDTO;
import pl.hellopoland.dto.EmailSendingReportDTO;
import pl.hellopoland.dto.FrequencyDataDTO;
import pl.hellopoland.dto.FrequencyTypeDTO;
import pl.hellopoland.dto.ImageDTO;
import pl.hellopoland.dto.LocationDTO;
import pl.hellopoland.dto.OpeningHoursDTO;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopoland.dto.SightEventDTO;
import pl.hellopoland.dto.StatusDTO;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketTypeDTO;
import pl.hellopoland.dto.TicketPoolDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopoland.dto.TicketPoolInfoDTO;
import pl.hellopoland.dto.TicketPoolTypeDTO;
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
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.FrequencyData;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketType;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.model.util.Discount;
import pl.hellopolandticket.security.CurrentUser;

public class ModelObjectsToDTOConverter {


  public static SightEventDTO ofSightEvent(SightEvent sightEvent, Long sightId) {
    SightEventDTO sightEventDTO = ofSightEventBasic(sightEvent);
    sightEventDTO.description = sightEvent.getDescription();
    sightEventDTO.duration = sightEvent.getDuration();
    sightEventDTO.lead = sightEvent.getLead();
    sightEventDTO.mainImage = new ImageDTO();
    sightEventDTO.mainImage.original = sightEvent.getMainImageUrl();
    sightEventDTO.email = sightEvent.getEmail();
    sightEventDTO.phone = sightEvent.getPhone();
    sightEventDTO.generalAdmission = sightEvent.getGeneralAdmission();
    sightEventDTO.sightId = sightId;
    sightEventDTO.location = ofNullable(sightEvent.getSightEventLocation())
        .map(ModelObjectsToDTOConverter::ofSightLocation).orElse(null);
    sightEventDTO.ticketPoolDefinitions = sightEvent.getTicketPoolDefinitions().stream()
        .map(ModelObjectsToDTOConverter::ofTicketPoolDefinition).collect(toList());
    sightEventDTO.openingHours = ofNullable(sightEvent.getOpeningHours())
        .map(v -> v.stream().map(ModelObjectsToDTOConverter::ofOpeningHours).collect(toList()))
        .orElse(null);
    return sightEventDTO;
  }

  public static SightEventDTO ofSightEventBasic(SightEvent sightEvent) {
    SightEventDTO sightEventDTO = new SightEventDTO();
    sightEventDTO.id = sightEvent.getId();
    sightEventDTO.name = sightEvent.getName();
    sightEventDTO.blocked = sightEvent.getBlocked();
    sightEventDTO.published = sightEvent.getPublished();
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
    location.directions = sightEventLocation.getDirections();
    return location;
  }

  public static TicketDefinitionDTO ofTicketDefinition(TicketDefinition ticketDefinition) {
    TicketDefinitionDTO ticketDefinitionDTO = new TicketDefinitionDTO();

    ticketDefinitionDTO.id = ticketDefinition.getId();
    ticketDefinitionDTO.name = ticketDefinition.getName();
    ticketDefinitionDTO.originalPrice = ticketDefinition.getPrice();
    ticketDefinitionDTO.partnerId = ticketDefinition.getPartner().getId();
    ticketDefinitionDTO.ticketTypeId = ticketDefinition.getTicketType().getId();
    ticketDefinitionDTO.ticketType = ofTicketType(ticketDefinition.getTicketType());
    AvailableTicketNumberAssociation atna = ticketDefinition.getInterestingAtna();
    if (atna != null) {
      ticketDefinitionDTO.atnaId = atna.getId();
      TicketPoolDefinition tpd = atna.getTicketPoolDefinition();
      ticketDefinitionDTO.sightEventId = tpd.getSightEvent().getId();
      ticketDefinitionDTO.poolId = tpd.getId();
        Discount discount = atna.getEffectiveDiscount();
        if (discount != null) {
            ticketDefinitionDTO.discount = ofDiscount(discount);
        }

    }

    ticketDefinitionDTO.calculatePrice();
    return ticketDefinitionDTO;
  }

  public static TicketTypeDTO ofTicketType(TicketType ticketType) {
    TicketTypeDTO dto = new TicketTypeDTO();
    dto.id = ticketType.getId();
    dto.code = ticketType.getCode();
    dto.label = ticketType.getLabel();
    dto.eligibleForPriceFrom = ticketType.isEligibleForPriceFrom();
    dto.active = ticketType.isActive();
    dto.sortOrder = ticketType.getSortOrder();
    return dto;
  }

  public static DiscountDTO ofDiscount(Discount discount) {
    DiscountDTO dto = new DiscountDTO();
    dto.type = DiscountTypeDTO.valueOf(discount.getType().name());
    dto.isCustomCommission = discount.isCustomComission();
    dto.value = discount.getValue();
    dto.amount = discount.getAmount();
    dto.percent = discount.getPercent();
    dto.price = discount.getDiscountPrice();
    dto.hplPart = discount.getHplPart();
    dto.partnerPart = discount.getPartnerPart();
    return dto;
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
    if (ticket.getDiscount() != null) {
      ticketDTO.discount = ofDiscount(ticket.getDiscount());
    }
    return ticketDTO;
  }

  public static TicketDTO ofTicketWithQrCode(Ticket ticket, ByteArrayOutputStream qrCode) {
    TicketDTO ticketDTO = ofTicket(ticket);
    ticketDTO.qrCode = qrCode;
    return ticketDTO;
  }

  public static TicketDTO ofTicketAfterPunch(Ticket ticket) {
    TicketDTO ticketDTO = ofTicket(ticket);
    TicketPoolInfoDTO tpiDTO = new TicketPoolInfoDTO();
    tpiDTO.punchedTicketCount = Long.valueOf(ticket.getTicketPool().getTickets().stream()
        .filter(t -> Status.PUNCHED.equals(t.getStatus())).count());
    tpiDTO.toBePunchedTicketCount = Long.valueOf(ticket.getTicketPool().getTickets().stream()
        .filter(t -> Status.BOUGHT.equals(t.getStatus())).count());
    tpiDTO.boughtTicketCount = tpiDTO.punchedTicketCount + tpiDTO.toBePunchedTicketCount;
    ticketDTO.ticketPoolInfo = tpiDTO;
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
    partnerDTO.email = partner.getEmail();
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
      String message, String code,Object object) {
    AbstractErrorDTO abstractErrorDTO = new AbstractErrorDTO();
    abstractErrorDTO.exception = exception.getSimpleName();
    abstractErrorDTO.message = message;
    abstractErrorDTO.code = code;
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
    TicketPoolDefinition ticketPoolDefinition = ticketPool.getTicketPoolDefinition();
    ticketPoolDTO.id = ticketPool.getId();
    ticketPoolDTO.name = ticketPool.getName();
    ticketPoolDTO.availableTicketsNumber = ticketPool.getAvailableTicketsNumber();
    ticketPoolDTO.startDate = ticketPool.getStartDate();
    ticketPoolDTO.endDate = ticketPool.getEndDate();
    ticketPoolDTO.entryStartDate = ticketPool.getEntryStartDate();
    ticketPoolDTO.entryEndDate = ticketPool.getEntryEndDate();
    ticketPoolDTO.ticketPoolDefinitionId =
        ofNullable(ticketPoolDefinition).map(tpd -> tpd.getId()).orElse(null);
    ticketPoolDTO.wholeDay = ticketPool.isWholeDay();
    ticketPoolDTO.isCyclic = ticketPoolDefinition.getIsCyclic();
    ticketPoolDTO.poolType = ofTicketPoolType(ticketPoolDefinition);
    ticketPoolDTO.visibleForPartner = ticketPoolDefinition.isVisibleForPartner();
    ticketPoolDTO.visibleOnPortal = ticketPoolDefinition.isVisibleOnPortal();
    ticketPoolDTO.ticketDefinitions =
              ticketPoolDefinition.getTicketDefinitions().stream()
                      .map(td -> ofTicketDefinitionForPool(td, ticketPool))
                      .collect(toList());
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
    ticketPoolDefinitionDTO.poolType = ofTicketPoolType(ticketPoolDefinition);
    ticketPoolDefinitionDTO.visibleForPartner = ticketPoolDefinition.isVisibleForPartner();
    ticketPoolDefinitionDTO.visibleOnPortal = ticketPoolDefinition.isVisibleOnPortal();
    ticketPoolDefinitionDTO.partnerId = ticketPoolDefinition.getSightEvent().getPartner().getId();
    return ticketPoolDefinitionDTO;
  }

  private static TicketPoolTypeDTO ofTicketPoolType(TicketPoolDefinition ticketPoolDefinition) {
    return ofNullable(ticketPoolDefinition.getPoolType())
        .map(poolType -> TicketPoolTypeDTO.valueOf(poolType.name()))
        .orElse(TicketPoolTypeDTO.STANDARD);
  }

    public static TicketPoolDefinitionDTO ofTicketPoolDefinition(
            TicketPoolDefinition ticketPoolDefinition) {

        TicketPoolDefinitionDTO dto =
                ofTicketPoolDefinitionBasic(ticketPoolDefinition);

        dto.ticketDefinitions =
                ticketPoolDefinition.getTicketDefinitions().stream()
                        .map(ModelObjectsToDTOConverter::ofTicketDefinition)
                        .collect(toList());


        return dto;
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

  public static EmailSendingReportDTO ofEmailSendingReport(EmailSendingReport report) {
    var dto = new EmailSendingReportDTO();
    if (report.validSentAddresses != null && report.validSentAddresses.length > 0) {
      dto.validSentAddresses = Arrays.stream(report.validSentAddresses)
          .map(address -> address.toString()).toArray(String[]::new);
    }
    if (report.validUnsentAddresses != null && report.validUnsentAddresses.length > 0) {
      dto.validUnsentAddresses = Arrays.stream(report.validUnsentAddresses)
          .map(address -> address.toString()).toArray(String[]::new);
    }
    if (report.invalidAddresses != null && report.invalidAddresses.length > 0) {
      dto.invalidAddresses = Arrays.stream(report.invalidAddresses)
          .map(address -> address.toString()).toArray(String[]::new);
    }
    return dto;
  }

    public static TicketDefinitionDTO ofTicketDefinitionForPool(
            TicketDefinition ticketDefinition,
            TicketPool ticketPool
    ) {
        TicketDefinitionDTO dto = new TicketDefinitionDTO();

        dto.id = ticketDefinition.getId();
        dto.name = ticketDefinition.getName();
        dto.originalPrice = ticketDefinition.getPrice();
        dto.partnerId = ticketDefinition.getPartner().getId();
        dto.ticketTypeId = ticketDefinition.getTicketType().getId();
        dto.ticketType = ofTicketType(ticketDefinition.getTicketType());

        AvailableTicketNumberAssociation atna =
                ticketDefinition.getInterestingAtnaForPool(ticketPool.getId());

        if (atna != null) {
            dto.atnaId = atna.getId();
            dto.sightEventId = ticketPool.getTicketPoolDefinition().getSightEvent().getId();
            dto.poolId = ticketPool.getId();

            Discount discount = atna.getEffectiveDiscount();
            if (discount != null) {
                dto.discount = ofDiscount(discount);
            }
        }
        dto.calculatePrice();
        return dto;
    }

}
