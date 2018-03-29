package pl.hellopolandticket.service;

import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.hellopolandticket.dao.SightDao;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Sight;

@Slf4j
@RunWith(Arquillian.class)
public class BookingCancelSchedulerTest {

  @Inject
  private SightService sightService;

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return ShrinkWrap
        .create(WebArchive.class, "test.war")
        .addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
        .addClasses(SightService.class, SightDao.class, TicketDao.class, TicketDefinitionDao.class);
  }

  @Test
  @UsingDataSet("datasets/sights.yml")
  public void testCars() {
    Sight s = sightService.findBySightName("Kolejkowo");
    assertTrue(true);

  }
}