package pl.hellopolandticket.service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class UserService {

  @Inject
  private UserDao userDao;

  @Inject
  private ExceptionFactory exceptionFactory;

  public User findByEmail(String email) {
    return userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }
}
