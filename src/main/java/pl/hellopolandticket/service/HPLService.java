package pl.hellopolandticket.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.event.HPLPushEvent;

@ApplicationScoped
public class HPLService extends ServiceSuperclass {

  private static final String SIGHT_EVENTS_UPLOAD_URL_PROPERTY = "rest.url.sightEventsUpload";

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private SightEventService sightEventService;

  @Inject
  private Event<HPLPushEvent> hplPushEvent;

  public void pushDataToHPL(CurrentUser currentUser) {
    hplPushEvent.fireAsync(HPLPushEvent.builder()
        .URLPath(
            applicationPropertyService.findByName(SIGHT_EVENTS_UPLOAD_URL_PROPERTY).propertyValue)
        .push(sightEventService.findAllAndConvertToPushDTOObject(currentUser)).build());
  }
}
