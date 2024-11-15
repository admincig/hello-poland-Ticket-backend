package pl.hellopolandticket.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Date;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import pl.hellopolandticket.annotation.DateFormat;
import pl.hellopolandticket.annotation.DateTimeFormat;

@Provider
public class DateParameterConverterProvider implements ParamConverterProvider {

  @SuppressWarnings("unchecked")
  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
      Annotation[] annotations) {
    if (Date.class.equals(rawType)) {
      final DateParameterConverter dateParameterConverter = new DateParameterConverter();

      for (Annotation annotation : annotations) {
        if (DateTimeFormat.class.equals(annotation.annotationType())) {
          dateParameterConverter.setCustomDateTimeFormat((DateTimeFormat) annotation);
        } else if (DateFormat.class.equals(annotation.annotationType())) {
          dateParameterConverter.setCustomDateFormat((DateFormat) annotation);
        }
      }
      return (ParamConverter<T>) dateParameterConverter;
    }
    return null;
  }

}
