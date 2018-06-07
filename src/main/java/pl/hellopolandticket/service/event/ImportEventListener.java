package pl.hellopolandticket.service.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.service.ApplicationPropertyService;
import pl.hellopolandticket.service.HttpClient;

@ApplicationScoped
@Interceptors(value = LoggingHandler.class)
public class ImportEventListener {

  @Inject
  private HttpClient httpClient;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  private static final String TICKETS_UPLOAD_URL_PROPERTY = "rest.url.ticketsUpload";

  public void ticketsImportEventHandler(
      @ObservesAsync TicketDefinitionsImportEvent ticketDefinitionsImportEvent) {
    httpClient.sendPostRequest(
        applicationPropertyService.findByName(TICKETS_UPLOAD_URL_PROPERTY).getPropertyValue(),
        ticketDefinitionsImportEvent.getTicketDefinitions());
  }
}