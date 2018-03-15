package pl.hellopolandticket.rest.util;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.util.List;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import pl.hellopolandticket.service.csv.pojo.SightCSV;
import pl.hellopolandticket.service.csv.pojo.TicketCSV;
import pl.hellopolandticket.service.exception.ImportingDataException;

public class FileToCSVParser {


  public static List<SightCSV> parseFileToSightsCSVList(byte[] sightsCSVFile) {
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
      throw new ImportingDataException();
    }
  }

  public static List<TicketCSV> parseFileToTicketsCSVList(byte[] ticketsCSVFile) {
    try {
      CsvMapper csvMapper = new CsvMapper();
      CsvSchema ticketSchema = csvMapper.schemaFor(TicketCSV.class).withNullValue("");

      MappingIterator<TicketCSV> ticketsMappingIterator = csvMapper.readerFor(TicketCSV.class)
          .with(ticketSchema)
          .readValues(ticketsCSVFile);

      List<TicketCSV> ticketsCSV = ticketsMappingIterator.readAll();

      validateCSVObjects(ticketsCSV.toArray());

      return ticketsCSV;
    } catch (Exception e) {
      throw new ImportingDataException();
    }
  }

  private static void validateCSVObjects(Object... objects) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    for (Object o : objects) {
      if (!validator.validate(o).isEmpty()) {
        throw new ImportingDataException();
      }
    }
  }
}
