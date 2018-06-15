package pl.hellopolandticket.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import pl.hellopolandticket.security.CurrentUser;

@ApplicationScoped
public class HPLService extends ServiceSuperclass {

  private static final String SIGHT_EVENTS_UPLOAD_URL_PROPERTY = "rest.url.sightEventsUpload";

  @Inject
  private HttpClient httpClient;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private SightEventService sightEventService;

  public void pushDataToHPL(CurrentUser currentUser) {
    httpClient.sendPostRequest(
        applicationPropertyService.findByName(SIGHT_EVENTS_UPLOAD_URL_PROPERTY).getPropertyValue(),
        sightEventService.findAllAndConvertToPushDTOObject(currentUser));
  }
}
