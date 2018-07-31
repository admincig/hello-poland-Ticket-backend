package pl.hellopolandticket.rest.exceptionhandler;

import static javax.ws.rs.core.Response.Status.CONFLICT;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class WrongTicketStatusExceptionHandler
    implements ExceptionMapper<WrongTicketStatusException> {

  @Override
  public Response toResponse(WrongTicketStatusException e) {
    return Response.status(CONFLICT).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
        .exception(e.getClass()).message(e.getMessage()).object(e.getTicket()).build()).build();
  }
}
