package pl.hellopolandticket.service;

import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofUser;
import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.UserAuthDTO;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.security.password.PasswordEncoder;
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
  private PasswordEncoder passwordEncoder;
  @Inject
  @Authenticated
  private CurrentUser currentUser;

  public User findUserById(Long id) {
    return userDao.findUserById(id).orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  public UserDTO findById(Long id) {
    return ofUser(findUserById(id));
  }

  public UserDTO getUserByRoleForCurrentPartner(long userId, String userRole) {
    return ofUser(
        userDao.getUserByRoleForCurrentPartner(userId, getLoggedUser().getPartner(), userRole));
  }

  public UserDTO getUserForCurrnetPartner(long userId) {
    return ofUser(userDao.getUserForCurrnetPartner(userId, getLoggedUser().getPartner()));
  }

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

  public void changePassword(UserAuthDTO userAuthDTO, Long userId) {
    User user = userId == null ? getLoggedUser() : findUserById(userId);
    // if (!passwordEncoder.matches(userAuthDTO.oldPassword, user.getPassword())) {
    // throw new ConflictingException("Incorrect old password.");
    // }
    user.setPassword(passwordEncoder.encode(userAuthDTO.password));
  }

  public List<UserDTO> getUshersForCurrnetPartner(CurrentUser currentUser) {
    User loggedUser = findUserByEmail(currentUser.getPrincipal());
    return userDao.getUsersByCurrentUserAndRole(loggedUser, Role.ROLE_USHER).stream()
        .map(ModelObjectsToDTOConverter::ofUser).collect(Collectors.toList());
  }

  public UserDTO updateUserForCurrnetPartner(UserDTO userDTO) {
    User user = userDao.getUserForCurrnetPartner(userDTO.id, getLoggedUser().getPartner());
    user.setName(userDTO.name);
    return ofUser(userDao.updateUser(user));
  }

}
