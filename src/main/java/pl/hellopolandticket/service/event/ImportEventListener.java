package pl.hellopolandticket.service.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import pl.hellopolandticket.service.HttpClient;

@ApplicationScoped
public class ImportEventListener {

  @Inject
  private HttpClient httpClient;

  private static final String ATTRACTIONS_UPLOAD_URL_PROPERTY = "rest.url.attractionsUpload";

  public void sightsImportEventHandler(@ObservesAsync SightsImportEvent sightsImportEvent) {
    httpClient
        .sendPostRequestWithAttractionsToURL(System.getProperty(ATTRACTIONS_UPLOAD_URL_PROPERTY),
            sightsImportEvent.getSights().toArray());
  }

  public void ticketsImportEventHandler(
      @ObservesAsync TicketDefinitionsImportEvent ticketDefinitionsImportEvent) {
    httpClient
        .sendPostRequestWithAttractionsToURL(System.getProperty(ATTRACTIONS_UPLOAD_URL_PROPERTY),
            ticketDefinitionsImportEvent.getTicketDefinitions().toArray());
  }
}