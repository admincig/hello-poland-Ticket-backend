package pl.hellopolandticket;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class BaseTest {

  @PersistenceContext(unitName = "test")
  protected EntityManager entityManager;

  @Deployment
  public static Archive<WebArchive> createDeployment() {
    return ShrinkWrap.create(WebArchive.class, "test.war").addAsResource("META-INF/persistence.xml")
        .addPackages(true, "pl.hellopolandticket");
  }

  @Before
  public void setup() {
    entityManager.getEntityManagerFactory().getCache().evictAll();
  }

  @SuppressWarnings("unchecked")
  public static <T extends List<?>> T castObjectToList(Object obj) {
    return (T) obj;
  }
}
