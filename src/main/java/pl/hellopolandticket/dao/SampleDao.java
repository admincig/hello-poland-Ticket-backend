package pl.hellopolandticket.dao;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import pl.hellopolandticket.model.Sample;

@Stateless
@LocalBean
public class SampleDao {

  @PersistenceContext
  private EntityManager entityManager;

  public String save() {
    entityManager.persist(Sample.builder()
        .name("Sample")
        .build());

    return "Sample";
  }
}
