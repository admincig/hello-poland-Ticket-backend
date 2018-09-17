package pl.hellopolandticket.service;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.OpeningHoursDao;
import pl.hellopolandticket.model.sightevent.OpeningHours;

@LocalBean
@Stateless
public class OpeningHoursService extends ServiceSuperclass {
  @Inject
  private OpeningHoursDao dao;

  public void persist(OpeningHours openingHours) {
    dao.persist(openingHours);
  }

  public void remove(List<OpeningHours> openingHours) {
    Optional.ofNullable(openingHours)
        .ifPresent(list -> list.stream().forEach(oh -> dao.remove(oh)));
  }

}
