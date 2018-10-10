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
  private static final String serverLogDir;
  static {
    String serverLogDirProp = System.getProperty("jboss.server.log.dir");
    serverLogDir = serverLogDirProp.endsWith("/") ? (serverLogDirProp) : (serverLogDirProp + "/");
  }
  private static final String ORDERS_LOG_DIR = serverLogDir + "helpdesk/orders/";
  private static final String TICKETS_LOG_DIR = serverLogDir + "helpdesk/tickets/";

  @GET
  @Path("/orders/{date}")
  public Response getOrdersLogs(@PathParam("date") String date) {
    String logPathStr = "";
    String now = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
    if (now.equals(date)) {
      logPathStr = ORDERS_LOG_DIR + "helpdesk-orders.log";
    } else {
      logPathStr = ORDERS_LOG_DIR + "helpdesk-orders.log." + date;
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

  @GET
  @Path("/tickets/{date}")
  public Response getticketsLogs(@PathParam("date") String date) {
    String logPathStr = "";
    String now = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
    if (now.equals(date)) {
      logPathStr = TICKETS_LOG_DIR + "helpdesk-tickets.log";
    } else {
      logPathStr = TICKETS_LOG_DIR + "helpdesk-tickets.log." + date;
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
