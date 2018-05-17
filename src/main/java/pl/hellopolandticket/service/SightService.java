package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.service.dto.SightDTO.ofSightBasic;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.PartnerDao;
import pl.hellopolandticket.dao.SightDao;
import pl.hellopolandticket.model.Partner;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.service.dto.SightDTO;

@Stateless
@LocalBean
public class SightService {

  @Inject
  private SightDao sightDao;

  @Inject
  private PartnerDao partnerDao;

  public Sight save(Sight sight) {
    return sightDao.persist(sight);
  }

  public SightDTO findById(Long sightId) {
    return ofSightBasic(sightDao.findById(sightId));
  }

  public List<SightDTO> findForPartner(String userLogin) {
    Partner partner = partnerDao.findUserEmail(userLogin);

    List<Long> sightIds = partner.getSights().stream()
        .map(Sight::getId)
        .collect(toList());

    return sightDao.findByIdsIn(sightIds).stream()
        .map(SightDTO::ofSightBasic)
        .collect(toList());
  }


}