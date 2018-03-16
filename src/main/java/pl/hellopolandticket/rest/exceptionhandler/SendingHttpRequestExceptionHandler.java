package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.SendingHttpRequestException;

@Provider
public class SendingHttpRequestExceptionHandler implements
    ExceptionMapper<SendingHttpRequestException> {

  @Override
  public Response toResponse(SendingHttpRequestException e) {
    return Response.status(BAD_REQUEST).entity(AbstractJSONError.builder()
        .exception(SendingHttpRequestException.class)
        .message(e.getMessage())
        .build()).build();
  }
}