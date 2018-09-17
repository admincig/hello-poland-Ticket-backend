package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.preconditionfailed.PreconditionFailedBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class PreconditionFailedExceptionHandler
    implements ExceptionMapper<PreconditionFailedBaseException> {

  @Override
  public Response toResponse(PreconditionFailedBaseException e) {
    return Response.status(PRECONDITION_FAILED).entity(ModelObjectsToDTOConverter
        .abstractErrorDTOBuilder().exception(e.getClass()).message(e.getMessage()).build()).build();
  }
}
