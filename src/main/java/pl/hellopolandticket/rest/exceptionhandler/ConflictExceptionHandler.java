package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.CONFLICT;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.conflict.ConflictBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class ConflictExceptionHandler implements
    ExceptionMapper<ConflictBaseException> {

  @Override
  public Response toResponse(ConflictBaseException e) {
    return Response.status(CONFLICT).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
        .exception(e.getClass())
        .message(e.getMessage())
        .build()).build();
  }
}
