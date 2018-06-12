package pl.hellopolandticket.rest;

import javax.interceptor.Interceptors;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import pl.hellopolandticket.app.LoggingHandler;

@Produces(MediaType.APPLICATION_JSON)
@Interceptors(value = LoggingHandler.class)
public class RestServiceSuperclass {

}
