package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.service.util.ModelObjectsToDTOConverter.ofApplicationProperty;

import java.util.List;
import java.util.Optional;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopoland.dto.ApplicationPropertyDTO;
import pl.hellopolandticket.dao.ApplicationPropertyDao;
import pl.hellopolandticket.model.config.ApplicationProperty;
import pl.hellopolandticket.service.exception.ExceptionFactory;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Stateless
@LocalBean
public class ApplicationPropertyService {

  @Inject
  private ApplicationPropertyDao applicationPropertyDao;

  @Inject
  private ExceptionFactory exceptionFactory;

  public ApplicationPropertyDTO setApplicationProperty(
      ApplicationPropertyDTO applicationPropertyDTO) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(applicationPropertyDTO.propertyName)
        .orElse(new ApplicationProperty());

    applicationProperty.setPropertyName(applicationPropertyDTO.propertyName);
    applicationProperty.setPropertyValue(applicationPropertyDTO.propertyValue);

    return ofApplicationProperty(applicationPropertyDao.persist(applicationProperty));
  }

  public ApplicationPropertyDTO findByName(String propertyName) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(propertyName)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());

    return ofApplicationProperty(applicationPropertyDao.persist(applicationProperty));
  }

  public Optional<ApplicationPropertyDTO> find(String propertyName) {
    return applicationPropertyDao
        .findByPropertyName(propertyName)
        .map(ModelObjectsToDTOConverter::ofApplicationProperty);
  }

  public void removeApplicationProperty(String propertyName) {
    ApplicationProperty applicationProperty = applicationPropertyDao
        .findByPropertyName(propertyName)
        .orElseThrow(() -> exceptionFactory.resourceNotFoundException());

    applicationPropertyDao.remove(applicationProperty);
  }

  public List<ApplicationPropertyDTO> findAll() {
    return applicationPropertyDao.findAll().stream()
        .map(ModelObjectsToDTOConverter::ofApplicationProperty)
        .collect(toList());
  }
}