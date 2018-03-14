package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.SightDao;
import pl.hellopolandticket.model.Sight;

@Stateless
@LocalBean
public class SightService {

  @Inject
  private SightDao sightDao;

  public Sight save(Sight sight) {
    return sightDao.persist(sight);
  }

  public Sight findById(Long id) {
    return sightDao.findById(id);
  }
}