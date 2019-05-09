package pl.hellopolandticket.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
// @Interceptors(value = LoggingHandler.class)
public class RestServiceSuperclass {

  @GET
  @Path("/ping")
  public Response ping() {
    return Response.ok().build();
  }

}
