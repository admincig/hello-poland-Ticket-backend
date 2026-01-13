package pl.hellopolandticket.rest.exceptionhandler;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.BaseException;
import pl.hellopolandticket.service.exception.ExceptionMessagesService;
import pl.hellopolandticket.service.exception.notfound.NotFoundBaseException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundBaseException> {

    @Inject
    private ExceptionMessagesService exceptionMessagesService;


    @Override

    public Response toResponse(NotFoundBaseException e) {
        String errorKey =
                (e instanceof BaseException && ((BaseException) e).getErrorKey() != null)
                        ? ((BaseException) e).getErrorKey()
                        : e.getClass().getSimpleName();

        return Response.status(NOT_FOUND).entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
            .exception(e.getClass()).message(exceptionMessagesService.getMessage(errorKey)).code(exceptionMessagesService.getCode(errorKey)).build()).build();
  }
}
