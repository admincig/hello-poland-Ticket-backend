package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.WrongTicketStatusException;

@Provider
public class WrongTicketStatusExceptionHandler implements
    ExceptionMapper<WrongTicketStatusException> {

  @Override
  public Response toResponse(WrongTicketStatusException e) {
    return Response.status(CONFLICT).entity(AbstractJSONError.builder()
        .exception(e.getClass())
        .message(e.getMessage())
        .object(e.getTicket())
        .build()).build();
  }
}
