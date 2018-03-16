package pl.hellopolandticket.service.csv;

import static java.util.stream.Collectors.toList;

import java.util.List;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.TicketService;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketCSV;
import pl.hellopolandticket.service.dto.ModelObjectsToDTOConverter;
import pl.hellopolandticket.service.event.SightsImportEvent;
import pl.hellopolandticket.service.event.TicketsImportEvent;
import pl.hellopolandticket.service.exception.ImportingDataException;

@Stateless
@LocalBean
public class CSVService {

  @Inject
  private SightService sightService;

  @Inject
  private TicketService ticketService;

  @Inject
  private Event<SightsImportEvent> sightsImportEvent;

  @Inject
  private Event<TicketsImportEvent> ticketsImportEvent;

  public void importSightsFromCSV(List<SightCSV> sightsCSV) {
    try {
      List<Sight> sights = sightsCSV.stream()
          .map(SightCSV::createSight)
          .collect(toList());

      sights.forEach(sight -> sightService.save(sight));

      sightsImportEvent.fire(createSightsImportEvent(sights));
    } catch (EJBTransactionRolledbackException e) {
      throw new ImportingDataException();
    }
  }

  public void importTicketsFromCSV(List<TicketCSV> ticketsCSV) {
    try {
      List<Ticket> tickets = ticketsCSV.stream()
          .map(this::createTicketOfTicketCSV)
          .collect(toList());

      tickets.forEach(ticket -> ticketService.save(ticket));

      ticketsImportEvent.fire(createTicketsImportEvent(tickets));
    } catch (EJBTransactionRolledbackException e) {
      throw new ImportingDataException();
    }
  }


  private Ticket createTicketOfTicketCSV(TicketCSV ticketCSV) {
    return Ticket.builder()
        .name(ticketCSV.getName())
        .price(ticketCSV.getPrice())
        .predefinedDate(ticketCSV.getPredefinedDate())
        .date(ticketCSV.getDate())
        .sight(sightService.findBySightName(ticketCSV.getSightName()))
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

  private TicketsImportEvent createTicketsImportEvent(List<Ticket> tickets) {
    List<pl.hellopoland.dto.Ticket> ticketsDTO = tickets.stream()
        .map(ModelObjectsToDTOConverter::ofTicket)
        .collect(toList());

    return TicketsImportEvent.builder()
        .tickets(ticketsDTO)
        .build();
  }
}