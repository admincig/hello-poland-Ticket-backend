package pl.hellopolandticket.service.event;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import pl.hellopolandticket.service.HttpClient;

@Stateless
@LocalBean
public class ImportEventListener {

  @Inject
  private HttpClient httpClient;

  private static final String ATTRACTIONS_UPLOAD_URL_PROPERTY = "rest.url.attractionsUpload";

  public void sightsImportEventHandler(@Observes SightsImportEvent sightsImportEvent) {
    httpClient
        .sendPostRequestWithAttractionsToURL(System.getProperty(ATTRACTIONS_UPLOAD_URL_PROPERTY),
            sightsImportEvent.getSights().toArray());
  }

  public void ticketsImportEventHandler(@Observes TicketsImportEvent ticketsImportEvent) {
    httpClient
        .sendPostRequestWithAttractionsToURL(System.getProperty(ATTRACTIONS_UPLOAD_URL_PROPERTY),
            ticketsImportEvent.getTickets().toArray());
  }


}
