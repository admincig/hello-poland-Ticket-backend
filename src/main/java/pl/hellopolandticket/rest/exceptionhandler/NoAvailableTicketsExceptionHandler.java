package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.NoAvailableTicketsException;

@Provider
public class NoAvailableTicketsExceptionHandler implements
    ExceptionMapper<NoAvailableTicketsException> {

  @Override
  public Response toResponse(NoAvailableTicketsException e) {
    return Response.status(BAD_REQUEST).entity(AbstractJSONError.builder()
        .exception(e.getClass())
        .message(e.getMessage())
        .build()).build();
  }
}
