package pl.hellopolandticket.service;

import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofUser;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class UserService extends ServiceSuperclass {

  @Inject
  private UserDao userDao;

  @Inject
  private ExceptionFactory exceptionFactory;

  public User findUserByEmail(String email) {
    return userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public UserDTO findByEmail(String email) {
    return ofUser(userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException()));
  }

  public User save(User user) {
    return userDao.persist(user);
  }
}
