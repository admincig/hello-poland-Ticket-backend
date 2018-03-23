package pl.hellopolandticket.service.dto;

import static pl.hellopolandticket.service.dto.SightDTO.ofSightOnlyId;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketStatus;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO implements Serializable {

  private static final long serialVersionUID = -3362200913434743101L;

  private Long id;
  private SightDTO sight;
  private String name;
  private Integer price;
  private Date date;
  private TicketStatus ticketStatus;
  private Long serialNumber;

  public static TicketDTO ofTicket(Ticket ticket) {
    return TicketDTO.builder()
        .id(ticket.getId())
        .sight(ofSightOnlyId(ticket.getSight()))
        .name(ticket.getName())
        .price(ticket.getPrice())
        .date(ticket.getDate())
        .ticketStatus(ticket.getTicketStatus())
        .serialNumber(ticket.getSerialNumber())
        .build();
  }
}
