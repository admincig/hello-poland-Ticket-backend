package pl.hellopolandticket.rest.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketDefinitionCSV;
import pl.hellopolandticket.service.exception.ExceptionFactory;

@RequestScoped
public class FileToCSVParser {

  @Inject
  private ExceptionFactory exceptionFactory;

  public List<SightCSV> parseFileToSightsCSVList(byte[] sightsCSVFile) {
    try {
      CsvMapper csvMapper = new CsvMapper();

      CsvSchema sightSchema = csvMapper.schemaFor(SightCSV.class).withNullValue("");

      MappingIterator<SightCSV> sightsMappingIterator = csvMapper.readerFor(SightCSV.class)
          .with(sightSchema)
          .readValues(sightsCSVFile);

      List<SightCSV> sightsCSV = sightsMappingIterator.readAll();

      validateCSVObjects(sightsCSV.toArray());

      return sightsCSV;
    } catch (Exception e) {
      throw exceptionFactory.importingDataException();
    }
  }

  public List<TicketDefinitionCSV> parseFileToTicketDefinitionsCSVList(
      byte[] ticketDefinitionsCSVFile) {
    try {
      CsvMapper csvMapper = new CsvMapper();
      CsvSchema ticketDefinitionSchema = csvMapper.schemaFor(TicketDefinitionCSV.class)
          .withNullValue("");

      MappingIterator<TicketDefinitionCSV> ticketDefinitionsMappingIterator = csvMapper
          .readerFor(TicketDefinitionCSV.class)
          .with(ticketDefinitionSchema)
          .readValues(ticketDefinitionsCSVFile);

      List<TicketDefinitionCSV> ticketDefinitionsCSV = ticketDefinitionsMappingIterator.readAll();

      validateCSVObjects(ticketDefinitionsCSV.toArray());

      return ticketDefinitionsCSV;
    } catch (Exception e) {
      throw exceptionFactory.importingDataException();
    }
  }

  private void validateCSVObjects(Object... objects) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    for (Object o : objects) {
      if (!validator.validate(o).isEmpty()) {
        throw exceptionFactory.importingDataException();
      }
    }
  }
}
