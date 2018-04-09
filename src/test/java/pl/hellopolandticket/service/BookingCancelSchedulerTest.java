package pl.hellopolandticket.service;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.hellopolandticket.model.Sight;

@Slf4j
@RunWith(Arquillian.class)
public class BookingCancelSchedulerTest {

  @Inject
  private SightService sightService;

  @PersistenceContext(unitName = "test")
  EntityManager entityManager;

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return ShrinkWrap
        .create(WebArchive.class, "test.war")
        .addAsResource("META-INF/persistence.xml")
        .addPackages(true, "pl.hellopolandticket");
  }

  @Test
  @UsingDataSet("datasets/import.yml")
  public void firstTest() {
    String kolejkowo = "Kolejkowo";
    Sight s = sightService.findBySightName(kolejkowo);
    assertEquals(kolejkowo, s.getName());

  }
}