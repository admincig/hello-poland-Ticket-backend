package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.NumberOfTicketsNotPositiveException;

@Provider
public class NumberOfTicketsNotPositiveExceptionHandler implements
    ExceptionMapper<NumberOfTicketsNotPositiveException> {

  @Override
  public Response toResponse(NumberOfTicketsNotPositiveException e) {
    return Response.status(PRECONDITION_FAILED).entity(AbstractJSONError.builder()
        .exception(e.getClass())
        .message(e.getMessage())
        .build()).build();
  }
}
