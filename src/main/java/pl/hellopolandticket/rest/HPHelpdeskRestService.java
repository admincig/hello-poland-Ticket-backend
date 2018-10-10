package pl.hellopolandticket.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/helpdesk")
@RequestScoped
public class HPHelpdeskRestService extends RestServiceSuperclass {
  private static final String LOG_DIR = "helpdesk/orders/";

  @GET
  @Path("/orders/{date}")
  public Response getLogs(@PathParam("date") String date) {
    String now = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());

    String logDir = System.getProperty("jboss.server.log.dir");
    logDir = logDir.endsWith("/") ? (logDir + LOG_DIR) : (logDir + "/" + LOG_DIR);

    String logPathStr = "";
    if (now.equals(date)) {
      logPathStr = logDir + "helpdesk-orders.log";
    } else {
      logPathStr = logDir + "helpdesk-orders.log." + date;
    }

    java.nio.file.Path logPath = Paths.get(logPathStr);
    if (Files.notExists(logPath)) {
      return Response.ok("Nie znaleziono logów dla podanej daty.").build();
    }

    try {
      var sb = new StringBuilder();
      Files.readAllLines(logPath).forEach(line -> sb.append(line).append(System.lineSeparator()));
      return Response.ok(sb.toString().length() == 0 ? "Brak wpisów." : sb.toString()).build();
    } catch (IOException e) {
      return Response.status(500, e.getMessage()).build();
    }
  }

}
