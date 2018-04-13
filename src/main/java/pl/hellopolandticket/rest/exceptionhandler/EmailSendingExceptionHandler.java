package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.EmailSendingException;
import pl.hellopolandticket.service.exception.ImportingDataException;

@Provider
public class EmailSendingExceptionHandler implements ExceptionMapper<EmailSendingException> {

  @Override
  public Response toResponse(EmailSendingException e) {
    return Response.status(BAD_REQUEST).entity(AbstractJSONError.builder()
        .exception(ImportingDataException.class)
        .message(e.getMessage())
        .build()).build();
  }
}
