package pl.hellopolandticket.rest;

import static pl.hellopolandticket.rest.util.FileToCSVParser.parseFileToSightsCSVList;
import static pl.hellopolandticket.rest.util.FileToCSVParser.parseFileToTicketDefinitionsCSVList;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.csv.CSVService;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketDefinitionCSV;

@Path("/csv")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class CSVRestService {

  @Inject
  private CSVService csvService;

  @POST
  @Path("/sights")
  public Response importSightsFromCSV(byte[] sightsCSVFile) {
    List<SightCSV> sightsCSV = parseFileToSightsCSVList(sightsCSVFile);

    csvService.importSightsFromCSV(sightsCSV);

    return Response.ok().build();
  }

  @POST
  @Path("/ticket-definitions")
  public Response importTicketDefinitionsFromCSV(byte[] ticketDefinitionsCSVFile) {
    List<TicketDefinitionCSV> ticketDefinitionsCSV = parseFileToTicketDefinitionsCSVList(
        ticketDefinitionsCSVFile);

    csvService.importTicketDefinitionsFromCSV(ticketDefinitionsCSV);

    return Response.ok().build();
  }
}