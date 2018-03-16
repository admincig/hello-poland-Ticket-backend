package pl.hellopolandticket.service.event;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import pl.hellopolandticket.service.HttpService;

@Stateless
@LocalBean
public class ImportEventListener {

  private static final String URLPath = "http://localhost:8080/hellopoland-ticket-0.0.1-SNAPSHOT/v1/sample/sample";

  @Inject
  private HttpService httpService;

  private static final String ATTRACTIONS_UPLOAD_URL_PROPERTY = "rest.url.attractionsUpload";

  public void sightsImportEventHandler(@Observes SightsImportEvent sightsImportEvent) {
    httpService
        .sendPostRequestWithAttractionsToURL(System.getProperty(ATTRACTIONS_UPLOAD_URL_PROPERTY),
            sightsImportEvent.getSights().toArray());
  }

  public void ticketsImportEventHandler(@Observes TicketsImportEvent ticketsImportEvent) {
    httpService
        .sendPostRequestWithAttractionsToURL(URLPath, ticketsImportEvent.getTickets().toArray());
  }


}
