package pl.hellopolandticket.service.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import pl.hellopolandticket.service.HttpClient;

@ApplicationScoped
public class ImportEventListener {

  @Inject
  private HttpClient httpClient;

  private static final String SIGHTS_UPLOAD_URL_PROPERTY = "rest.url.sightsUpload";

  private static final String TICKETS_UPLOAD_URL_PROPERTY = "rest.url.ticketsUpload";

  public void sightsImportEventHandler(@ObservesAsync SightsImportEvent sightsImportEvent) {
    httpClient
        .sendPostRequest(System.getProperty(SIGHTS_UPLOAD_URL_PROPERTY),
            sightsImportEvent.getSights());
  }

  public void ticketsImportEventHandler(
      @ObservesAsync TicketDefinitionsImportEvent ticketDefinitionsImportEvent) {
    httpClient
        .sendPostRequest(System.getProperty(TICKETS_UPLOAD_URL_PROPERTY),
            ticketDefinitionsImportEvent.getTicketDefinitions());
  }
}