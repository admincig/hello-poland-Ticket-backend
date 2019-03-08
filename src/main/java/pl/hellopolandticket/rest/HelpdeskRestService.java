package pl.hellopolandticket.rest;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.service.PartnerService;

@Path("/helpdesk")
@RequestScoped
public class HelpdeskRestService extends RestServiceSuperclass {
  private static final String serverLogDir;
  static {
    String serverLogDirProp = System.getProperty("jboss.server.log.dir");
    serverLogDir = serverLogDirProp.endsWith("/") ? (serverLogDirProp) : (serverLogDirProp + "/");
  }
  private static final String ORDERS_LOG_PATH =
      serverLogDir + "helpdesk/orders/helpdesk-orders.log";
  private static final String TICKETS_LOG_PATH =
      serverLogDir + "helpdesk/tickets/helpdesk-tickets.log";

  @Inject
  private PartnerService partnerService;

  @POST
  @Path("/partners")
  @RolesAllowed({ROLE_ADMIN})
  public Response add(PartnerDTO partner) {
    return Response.ok(partnerService.save(partner)).build();
  }

  @GET
  @Path("/orders/{date}")
  public Response getOrdersLogs(@PathParam("date") String date) {
    return getLogResponse(date, ORDERS_LOG_PATH);
  }

  @GET
  @Path("/tickets/{date}")
  public Response getticketsLogs(@PathParam("date") String date) {
    return getLogResponse(date, TICKETS_LOG_PATH);
  }

  private Response getLogResponse(String date, String path) {
    String logPathStr = "";
    String now = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
    if (now.equals(date)) {
      logPathStr = path;
    } else {
      logPathStr = path + "." + date;
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
