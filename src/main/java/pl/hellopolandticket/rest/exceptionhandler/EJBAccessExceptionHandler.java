package pl.hellopolandticket.rest.exceptionhandler;

import java.lang.System.Logger.Level;
import jakarta.ejb.EJBAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EJBAccessExceptionHandler implements ExceptionMapper<EJBAccessException> {

  private System.Logger logger = System.getLogger(EJBAccessExceptionHandler.class.getName());

  @Context
  HttpServletRequest ctx;

  @Override
  public Response toResponse(EJBAccessException exception) {
    if (ctx.getUserPrincipal() == null) {
      logger.log(Level.WARNING,
          "401. Registered unauthenticated try to access protected resources: "
              + exception.getMessage());
      return Response.status(Status.UNAUTHORIZED).build();
    }
    logger.log(Level.WARNING, "403. Registered unauthorized try to access protected resources: "
        + exception.getMessage());
    return Response.status(Status.FORBIDDEN).build();
  }
}
