package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
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

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void persist(OpeningHours openingHours) {
    dao.persist(openingHours);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void remove(List<OpeningHours> openingHours) {
    Optional.ofNullable(openingHours)
        .ifPresent(list -> list.stream().forEach(oh -> dao.remove(oh)));
  }

}
