package pl.hellopolandticket.model.converter;

import java.util.concurrent.atomic.AtomicInteger;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class AtomicIntegerToIntegerConverter implements AttributeConverter<AtomicInteger, Integer> {


  @Override
  public Integer convertToDatabaseColumn(AtomicInteger atomicInteger) {
    return atomicInteger.get();
  }

  @Override
  public AtomicInteger convertToEntityAttribute(Integer integer) {
    return new AtomicInteger(integer);
  }
}
