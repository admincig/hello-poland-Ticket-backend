package pl.hellopolandticket.rest.exceptionhandler;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.badrequest.BadRequestBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class BadRequestExceptionHandler implements ExceptionMapper<BadRequestBaseException> {

  @Override
  public Response toResponse(BadRequestBaseException e) {
    return Response.status(BAD_REQUEST).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
        .exception(e.getClass()).message(e.getMessage()).build()).build();
  }
}
