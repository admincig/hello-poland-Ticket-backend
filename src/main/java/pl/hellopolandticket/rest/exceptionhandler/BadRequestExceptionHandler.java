package pl.hellopolandticket.rest.exceptionhandler;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.BaseException;
import pl.hellopolandticket.service.exception.ExceptionMessagesService;
import pl.hellopolandticket.service.exception.badrequest.BadRequestBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class BadRequestExceptionHandler implements ExceptionMapper<BadRequestBaseException> {

    @Inject
    private ExceptionMessagesService exceptionMessagesService;

    @Override
    public Response toResponse(BadRequestBaseException e) {
        String errorKey =
                (e instanceof BaseException && ((BaseException) e).getErrorKey() != null)
                        ? ((BaseException) e).getErrorKey()
                        : e.getClass().getSimpleName();

        return Response.status(BAD_REQUEST).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
        .exception(e.getClass()).message(exceptionMessagesService.getMessage(errorKey)).code(exceptionMessagesService.getCode(errorKey)).build()).build();
  }
}
