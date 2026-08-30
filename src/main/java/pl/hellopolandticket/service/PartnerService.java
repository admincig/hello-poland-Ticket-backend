package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.Role.ROLE_SALESMAN;
import static pl.hellopolandticket.model.auth.Role.ROLE_USHER;
import static pl.hellopolandticket.model.auth.User.createHiddenUser;
import static pl.hellopolandticket.model.auth.User.createUsher;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofPartner;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofPartnerWithToken;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.persistence.PersistenceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopoland.dto.RoleDTO;
import pl.hellopoland.dto.UserDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;
import pl.hellopolandticket.security.password.PasswordEncoder;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;

@Stateless
@LocalBean
public class PartnerService extends ServiceSuperclass {
  @Inject
  private PartnerDao partnerDao;
  @Inject
  private UserService userService;
  @Inject
  private UserDao userDao;
  @Inject
  private PasswordEncoder passwordEncoder;
  @Inject
  private EmailSenderService emailService;
  @Inject
  private ExceptionFactory exceptionFactory;

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public PartnerDTO save(PartnerDTO partner) {
    if (partnerDao.findByName(partner.name) != null || partnerDao.findByEmail(partner.email) != null) {
      throw exceptionFactory.partnerAlreadyExistsException();
    }

    Partner partnerToPersist = Partner.builder().name(partner.name).email(partner.email).build();
    try {
      partnerDao.persist(partnerToPersist);
      partnerDao.flush();
    } catch (PersistenceException e) {
      if (isConstraintViolation(e)) {
        throw exceptionFactory.partnerAlreadyExistsException();
      }
      throw e;
    }
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

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public PartnerDTO update(Long partnerId, PartnerDTO partnerDTO) {
    String name = StringUtils.trimToNull(partnerDTO == null ? null : partnerDTO.name);
    String email = StringUtils.trimToNull(partnerDTO == null ? null : partnerDTO.email);
    if (name == null || email == null) {
      throw new ConflictingException("Nazwa i e-mail partnera nie mogą być puste.");
    }
    email = email.toLowerCase(Locale.ROOT);

    Partner partner = partnerDao.findById(partnerId)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
    Partner partnerWithName = partnerDao.findByName(name);
    Partner partnerWithEmail = partnerDao.findByEmail(email);
    if ((partnerWithName != null && !Objects.equals(partnerWithName.getId(), partnerId))
        || (partnerWithEmail != null && !Objects.equals(partnerWithEmail.getId(), partnerId))) {
      throw exceptionFactory.partnerAlreadyExistsException();
    }

    User externalUser = userDao.findExternalUserForPartner(partner)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
    userDao.findByEmail(email)
        .filter(user -> !Objects.equals(user.getId(), externalUser.getId()))
        .ifPresent(user -> {
          throw exceptionFactory.partnerAlreadyExistsException();
        });

    partner.setName(name);
    partner.setEmail(email);
    externalUser.setName(name);
    externalUser.setEmail(email);
    try {
      partnerDao.flush();
    } catch (PersistenceException e) {
      if (isConstraintViolation(e)) {
        throw exceptionFactory.partnerAlreadyExistsException();
      }
      throw e;
    }
    return ofPartner(partner);
  }

  @RolesAllowed({ROLE_ADMIN, ROLE_SALESMAN})
  public void removeNewCreatedPartner(String partnerEmail) {
    Partner partner = partnerDao.findByUserEmail(partnerEmail);
    userService.removeAllUsersForPartner(partner);
    partnerDao.removeNewCreatedPartner(partner);
  }

  private boolean isAtLeastOneUsher(List<UserDTO> usersDTOs) {
    return usersDTOs.stream().anyMatch(user -> user.roles.contains(RoleDTO.USHER));
  }

  private boolean isConstraintViolation(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      if ("org.hibernate.exception.ConstraintViolationException"
          .equals(current.getClass().getName())) {
        return true;
      }
      current = current.getCause();
    }
    return false;
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
