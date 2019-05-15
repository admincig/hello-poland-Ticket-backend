package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_SALESMAN;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.model.auth.User.createHiddenUser;
import static pl.hellopolandticket.model.auth.User.createUsher;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofPartnerWithToken;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.security.RolesAllowed;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopoland.dto.RoleDTO;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.security.password.PasswordEncoder;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@Stateless
@LocalBean
public class PartnerService extends ServiceSuperclass {
  @Inject
  private PartnerDao partnerDao;
  @Inject
  private UserService userService;
  @Inject
  private PasswordEncoder passwordEncoder;
  @Inject
  private EmailService emailService;
  @Inject
  private ExceptionFactory exceptionFactory;

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public PartnerDTO save(PartnerDTO partner) {
    Partner partnerToPersist = Partner.builder().name(partner.name).email(partner.email).build();
    partnerDao.persist(partnerToPersist);
    User user = createHiddenUser(partner.name, partner.email,
        Set.of(ROLE_EXTERNAL_USER, ROLE_USHER), partnerToPersist);
    user.setPassword(passwordEncoder.encode(partner.password));
    user = userService.save(user);
    // creating partner's ushers:
    List<UserDTO> users = partner.users;
    if (users != null && !users.isEmpty() && isAtLeastOneUsher(users)) {
      saveUshers(users, partnerToPersist);
    }
    return ofPartnerWithToken(partnerToPersist, user.getToken());
  }

  private boolean isAtLeastOneUsher(List<UserDTO> usersDTOs) {
    return usersDTOs.stream().anyMatch(user -> user.roles.contains(RoleDTO.USHER));
  }

  private void saveUshers(List<UserDTO> users, Partner partner) {
    var emailPassword = new HashMap<String, String>();
    users.stream().filter(u -> u.roles.contains(RoleDTO.USHER)).forEach(u -> {
      String pass = RandomStringUtils.randomAlphanumeric(10);
      var usher = createUsher(u.name, u.email, passwordEncoder.encode(pass), partner);
      userService.save(usher);
      emailPassword.put(u.email, pass);
    });
    // sending emails to ushers (with theirs login and password):
    emailPassword.forEach((key, value) -> {
      try {
        emailService.sendSimpleEmail(key, "Nowe konto w Hello Poland. Bileter",
            "Twój login to " + key + ", hasło to " + value);
      } catch (MessagingException | UnsupportedEncodingException e) {
        throw exceptionFactory.emailSendingRollbackException();
      }
    });
  }

}
