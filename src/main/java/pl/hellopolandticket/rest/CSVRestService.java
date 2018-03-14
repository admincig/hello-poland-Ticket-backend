package pl.hellopolandticket.rest;

import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.csv.CSVService;

@Path("/csv")
@RequestScoped
public class CSVRestService {

  @Inject
  private CSVService csvService;

  @POST
  @Path("/sights")
  public Response importSightsFromCSV(byte[] sightsCSV) throws IOException {
    csvService.importSightsFromCSV(sightsCSV);

    return Response.ok().build();
  }

  @POST
  @Path("/tickets")
  public Response importTicketsFromCSV(byte[] ticketsCSV) throws IOException {
    csvService.importTicketsFromCSV(ticketsCSV);

    return Response.ok().build();
  }
}