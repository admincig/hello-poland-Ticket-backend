package pl.hellopolandticket.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HPLService {

  private static final String SIGHTS_UPLOAD_URL_PROPERTY = "rest.url.sightsUpload";

  @Inject
  private HttpClient httpClient;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private SightEventService sightEventService;

  public void pushDataToHPL() {
    httpClient.sendPostRequest(
        applicationPropertyService.findByName(SIGHTS_UPLOAD_URL_PROPERTY).getPropertyValue(),
        sightEventService.findAllAndConvertToDTOObject());
  }
}
