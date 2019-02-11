package pl.hellopolandticket.service;

import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofUser;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class UserService extends ServiceSuperclass {
  @Inject
  private UserDao userDao;
  @Inject
  private ExceptionFactory exceptionFactory;
  @Inject
  private PartnerDao partnerDao;

  public User findUserByEmail(String email) {
    return userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public UserDTO findByEmail(String email) {
    return ofUser(
        userDao.findByEmail(email).orElseThrow(() -> exceptionFactory.resourceNotFoundException()));
  }

  public User save(User user) {
    return userDao.persist(user);
  }

  public List<UserDTO> getUshers(CurrentUser currentUser) {
    Partner partner = partnerDao.findByUserEmail(currentUser.getPrincipal());
    return userDao.getUsersByPartnerAndRole(partner, Role.ROLE_USHER).stream()
        .map(ModelObjectsToDTOConverter::ofUser).collect(Collectors.toList());
  }
}
