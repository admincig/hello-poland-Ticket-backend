package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.notfound.NotFoundBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundBaseException> {

  @Override
  public Response toResponse(NotFoundBaseException e) {
    return Response.status(NOT_FOUND).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
        .exception(e.getClass())
        .message(e.getMessage())
        .build()).build();
  }
}
