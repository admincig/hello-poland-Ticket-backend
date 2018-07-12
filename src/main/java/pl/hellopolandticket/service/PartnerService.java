package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.auth.User.createHiddenUser;
import static pl.hellopolandticket.model.util.UUIDGeneratorUtil.generateUUID;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofPartnerWithToken;

import java.util.Collections;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.PartnerDTO;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.model.partner.Partner;

@Stateless
@LocalBean
public class PartnerService extends ServiceSuperclass {

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private UserService userService;

  public PartnerDTO save(PartnerDTO partner) {
    Partner partnerToPersist = Partner.builder()
        .name(partner.name)
        .build();

    partnerDao.persist(partnerToPersist);

    User user = createHiddenUser(generateUUID(),
        (partner.name + "@" + partner.name + ".com").replace(" ", ""),
        Collections.singleton(ROLE_EXTERNAL_USER),
        partnerToPersist);

    user = userService.save(user);

    return ofPartnerWithToken(partnerToPersist, user.getToken());
  }
}
