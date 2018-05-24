package pl.hellopolandticket.service;

import static pl.hellopolandticket.service.dto.PartnerDTO.ofPartnerWithToken;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.service.dto.PartnerDTO;

@Stateless
@LocalBean
public class PartnerService {

  @Inject
  private PartnerDao partnerDao;

  public PartnerDTO add(PartnerDTO partner) {
    Partner partnerToPersist = Partner.builder()
        .name(partner.getName())
        .build();

    partnerDao.persist(partnerToPersist);

    return ofPartnerWithToken(partnerToPersist);
  }
}
