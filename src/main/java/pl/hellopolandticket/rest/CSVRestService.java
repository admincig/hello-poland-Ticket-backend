package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.Role.ROLE_USER;

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.rest.util.FileToCSVParser;
import pl.hellopolandticket.service.csv.CSVService;
import pl.hellopolandticket.service.csv.pojo.TicketDefinitionCSV;

@Path("/csv")
@RequestScoped
public class CSVRestService extends RestServiceSuperclass {

  @Inject
  private CSVService csvService;

  @Inject
  private FileToCSVParser fileToCSVParser;

  @POST
  @Path("/ticket-definitions")
  @RolesAllowed({ROLE_USER, ROLE_EXTERNAL_USER})
  public Response importTicketDefinitionsFromCSV(byte[] ticketDefinitionsCSVFile) {
    List<TicketDefinitionCSV> ticketDefinitionsCSV = fileToCSVParser
        .parseFileToTicketDefinitionsCSVList(
            ticketDefinitionsCSVFile);

    csvService.importTicketDefinitionsFromCSV(ticketDefinitionsCSV);

    return Response.ok().build();
  }
}