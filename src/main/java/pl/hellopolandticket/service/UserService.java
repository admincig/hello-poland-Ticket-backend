package pl.hellopolandticket.service;

import static pl.hellopolandticket.service.dto.UserDTO.ofUserBasic;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.service.dto.UserDTO;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
@Interceptors(value = LoggingHandler.class)
public class UserService {

  @Inject
  private UserDao userDao;

  @Inject
  private ExceptionFactory exceptionFactory;

  public User findUserByEmail(String email) {
    return userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public UserDTO findByEmail(String email) {
    return ofUserBasic(userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException()));
  }

  public User save(User user) {
    return userDao.persist(user);
  }
}
