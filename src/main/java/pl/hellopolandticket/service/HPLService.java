package pl.hellopolandticket.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;

@ApplicationScoped
@Interceptors(value = LoggingHandler.class)
public class HPLService {

  private static final String SIGHT_EVENTS_UPLOAD_URL_PROPERTY = "rest.url.sightEventsUpload";

  @Inject
  private HttpClient httpClient;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private SightEventService sightEventService;

  public void pushDataToHPL() {
    httpClient.sendPostRequest(
        applicationPropertyService.findByName(SIGHT_EVENTS_UPLOAD_URL_PROPERTY).getPropertyValue(),
        sightEventService.findAllAndConvertToDTOObject());
  }
}
