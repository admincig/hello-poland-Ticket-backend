package pl.hellopolandticket.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
// @Interceptors(value = LoggingHandler.class)
public class RestServiceSuperclass {

  @GET
  @Path("/ping")
  public Response ping() {
    return Response.ok().entity("pong").build();
  }

}
