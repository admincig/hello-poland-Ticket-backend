package pl.hellopolandticket.service;

import static pl.hellopolandticket.model.Role.ROLE_EXTERNAL_USER;
import static pl.hellopolandticket.model.UUIDGeneratorUtil.generateUUID;
import static pl.hellopolandticket.model.User.createHiddenUser;
import static pl.hellopolandticket.service.dto.PartnerDTO.ofPartnerWithToken;

import java.util.Collections;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.User;
import pl.hellopolandticket.service.dto.PartnerDTO;

@Stateless
@LocalBean
public class PartnerService {

  @Inject
  private PartnerDao partnerDao;

  @Inject
  private UserService userService;

  public PartnerDTO save(PartnerDTO partner) {
    Partner partnerToPersist = Partner.builder()
        .name(partner.getName())
        .build();

    partnerDao.persist(partnerToPersist);

    User user = createHiddenUser(generateUUID(),
        (partner.getName() + "@" + partner.getName() + ".com").replace(" ", ""),
        Collections.singleton(ROLE_EXTERNAL_USER),
        partnerToPersist);

    user = userService.save(user);

    return ofPartnerWithToken(partnerToPersist, user.getToken());
  }
}
