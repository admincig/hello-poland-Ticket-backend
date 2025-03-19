package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_SALESMAN;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofUser;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import pl.hellopoland.dto.UserAuthDTO;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.Role;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
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

  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_USHER})
  public User findUserById(Long id) {
    return userDao.findUserById(id).orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_USHER})
  public UserDTO findById(Long id) {
    return ofUser(findUserById(id));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public UserDTO getUserByRoleForCurrentPartner(long userId, String userRole) {
    return ofUser(
        userDao.getUserByRoleForCurrentPartner(userId, getLoggedUser().getPartner(), userRole));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public UserDTO getUserForCurrnetPartner(long userId) {
    return ofUser(userDao.getUserForCurrnetPartner(userId, getLoggedUser().getPartner()));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_USHER})
  public User findUserByEmail(String email) {
    return userDao.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
  }

  @PermitAll
  public UserDTO findByEmail(String email) {
    return ofUser(
        userDao.findByEmail(email).orElseThrow(() -> exceptionFactory.resourceNotFoundException()));
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public User save(User user) {
    return userDao.persist(user);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public void changePassword(UserAuthDTO userAuthDTO, Long userId) {
    User user = userId == null ? getLoggedUser() : findUserById(userId);
    if (StringUtils.isNotBlank(userAuthDTO.login) && !StringUtils.equals(user.getEmail(), userAuthDTO.login)) {
      user.setEmail(userAuthDTO.login);
    }
    user.setPassword(passwordEncoder.encode(userAuthDTO.password));
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public List<UserDTO> getUshersForCurrentPartner(CurrentUser currentUser) {
    User loggedUser = findUserByEmail(currentUser.getPrincipal());
    return userDao.getUsersByCurrentUserAndRole(loggedUser, Role.ROLE_USHER).stream()
        .map(ModelObjectsToDTOConverter::ofUser).collect(Collectors.toList());
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public UserDTO updateUserForCurrentPartner(UserDTO userDTO) {
    User user = userDao.getUserForCurrnetPartner(userDTO.id, getLoggedUser().getPartner());
    user.setName(userDTO.name);
    return ofUser(userDao.updateUser(user));
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public void removeAllUsersForPartner(Partner partner) {
    userDao.removeAllUsersForPartner(partner);
  }

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public UserDTO createUsher(UserDTO usher) {
    String hashedPassword = passwordEncoder.encode(usher.password);
    User user = User.createUsher(usher.name, usher.email, hashedPassword, getLoggedUser().getPartner());
    return ofUser(userDao.persist(user));
  }

}
