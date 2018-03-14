package pl.hellopolandticket.service.csv;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.IOException;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.SightService;
import pl.hellopolandticket.service.TicketService;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketCSV;

@Stateless
@LocalBean
public class CSVService {

  private final CsvMapper csvMapper;

  @Inject
  private SightService sightService;

  @Inject
  private TicketService ticketService;

  public CSVService() {
    csvMapper = new CsvMapper();
  }

  public void importSightsFromCSV(byte[] sightsCSV) throws IOException {
    CsvSchema sightSchema = csvMapper.schemaFor(SightCSV.class);

    MappingIterator<SightCSV> sightsMappingIterator = csvMapper.readerFor(SightCSV.class)
        .with(sightSchema)
        .readValues(sightsCSV);

    List<Sight> sights = sightsMappingIterator.readAll().stream()
        .map(SightCSV::createSight)
        .collect(toList());

    sights.forEach(sight -> sightService.save(sight));
  }

  public void importTicketsFromCSV(byte[] ticketsCSV) throws IOException {
    CsvSchema ticketSchema = csvMapper.schemaFor(TicketCSV.class);

    MappingIterator<TicketCSV> ticketsMappingIterator = csvMapper.readerFor(TicketCSV.class)
        .with(ticketSchema)
        .readValues(ticketsCSV);

    List<Ticket> tickets = ticketsMappingIterator.readAll().stream()
        .map(this::createTicketOfTicketCSV)
        .collect(toList());

    tickets.forEach(ticket -> ticketService.save(ticket));
  }

  private Ticket createTicketOfTicketCSV(TicketCSV ticketCSV) {
    return Ticket.builder()
        .name(ticketCSV.getName())
        .price(ticketCSV.getPrice())
        .predefinedDate(ticketCSV.getPredefinedDate())
        .date(ticketCSV.getDate())
        .sight(sightService.findById(ticketCSV.getSight()))
        .build();
  }
}