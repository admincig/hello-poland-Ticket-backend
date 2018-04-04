package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.service.dto.ApplicationPropertyDTO.ofApplicationProperty;

import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.ApplicationPropertyDao;
import pl.hellopolandticket.model.ApplicationProperty;
import pl.hellopolandticket.service.dto.ApplicationPropertyDTO;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Stateless
@LocalBean
public class ApplicationPropertyService {

  @Inject
  private ApplicationPropertyDao applicationPropertyDao;

  public ApplicationPropertyDTO setApplicationProperty(
      ApplicationPropertyDTO applicationPropertyDTO) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(applicationPropertyDTO.getPropertyName())
        .orElse(new ApplicationProperty());

    applicationProperty.setPropertyName(applicationPropertyDTO.getPropertyName());
    applicationProperty.setPropertyValue(applicationPropertyDTO.getPropertyValue());

    return ofApplicationProperty(applicationPropertyDao.persist(applicationProperty));
  }

  public ApplicationPropertyDTO findByName(String propertyName) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(propertyName)
        .orElseThrow(ResourceNotFoundException::new);

    return ofApplicationProperty(applicationPropertyDao.persist(applicationProperty));
  }

  public void removeApplicationProperty(String propertyName) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(propertyName)
        .orElseThrow(ResourceNotFoundException::new);

    applicationPropertyDao.remove(applicationProperty);
  }

  public List<ApplicationPropertyDTO> findAll() {
    return applicationPropertyDao.findAll().stream()
        .map(ApplicationPropertyDTO::ofApplicationProperty)
        .collect(toList());
  }
}