package pl.hellopolandticket.service.dto;

import static pl.hellopolandticket.model.Sight.UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE;
import static pl.hellopolandticket.model.TicketStatus.BOOKED;
import static pl.hellopolandticket.model.TicketStatus.BOUGHT;
import static pl.hellopolandticket.model.TicketStatus.PUNCHED;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.SightLocation;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketStatus;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SightDTO {

  private Long id;
  private String name;
  private Date date;
  private String lead;
  private String description;
  private String mainImageUrl;
  private String email;
  private String phone;
  private Integer availableTicketsNumber;
  private SightLocation sightLocation;

  private Integer totalTicketsNumber;
  private Integer punchedTicketsNumber;

  public static SightDTO ofSightOnlyId(Sight sight) {
    return SightDTO.builder()
        .id(sight.getId())
        .build();
  }

  public static SightDTO ofSightBasic(Sight sight) {
    int totalTicketNumber = sight.getAvailableTicketsNumber().get();
    int punchedTicketsNumber = 0;

    for (Ticket ticket : sight.getTickets()) {
      if (isValidTicket(ticket.getTicketStatus())) {
        if (ticket.getTicketStatus() == PUNCHED) {
          punchedTicketsNumber++;
        }

        if (totalTicketNumber != UNLIMITED_NUMBER_OF_AVAILABLE_TICKETS_VALUE) {
          totalTicketNumber++;
        }
      }
    }

    return SightDTO.builder()
        .id(sight.getId())
        .name(sight.getName())
        .date(sight.getDate())
        .totalTicketsNumber(totalTicketNumber)
        .punchedTicketsNumber(punchedTicketsNumber)
        .build();

  }

  private static boolean isValidTicket(TicketStatus ticketStatus) {
    return ticketStatus == BOOKED || ticketStatus == BOUGHT || ticketStatus == PUNCHED;
  }
}
