package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.dto.AbstractJSONError;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;

@Provider
public class ResourceNotFoundExceptionHandler implements
    ExceptionMapper<ResourceNotFoundException> {

  @Override
  public Response toResponse(ResourceNotFoundException e) {
    return Response.status(BAD_REQUEST).entity(AbstractJSONError.builder()
        .exception(e.getClass())
        .message(e.getMessage())
        .build()).build();
  }
}
