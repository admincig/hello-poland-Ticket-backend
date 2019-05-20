package pl.hellopolandticket.rest.exceptionhandler;

import java.lang.System.Logger.Level;
import javax.ejb.EJBAccessException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
