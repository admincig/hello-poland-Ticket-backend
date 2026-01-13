package pl.hellopolandticket.rest.exceptionhandler;

import static jakarta.ws.rs.core.Response.Status.CONFLICT;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pl.hellopolandticket.service.exception.BaseException;
import pl.hellopolandticket.service.exception.ExceptionMessagesService;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.util.ModelObjectsToDTOConverter;

@Provider
public class WrongTicketStatusExceptionHandler
        implements ExceptionMapper<WrongTicketStatusException> {

    @Inject
    private ExceptionMessagesService exceptionMessagesService;

    @Override
    public Response toResponse(WrongTicketStatusException e) {

        String errorKey =
                (e instanceof BaseException && ((BaseException) e).getErrorKey() != null)
                        ? ((BaseException) e).getErrorKey()
                        : e.getClass().getSimpleName();

        return Response.status(CONFLICT)
                .entity(ModelObjectsToDTOConverter.abstractErrorDTOBuilder()
                        .exception(e.getClass())
                        .message(exceptionMessagesService.getMessage(errorKey))
                        .code(exceptionMessagesService.getCode(errorKey))
                        .object(e.getTicket())
                        .build())
                .build();
    }
}

