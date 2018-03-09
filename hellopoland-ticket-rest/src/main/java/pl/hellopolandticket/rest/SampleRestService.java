package pl.hellopolandticket.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import pl.hellopolandticket.service.SampleService;

@Path("/sample")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
public class SampleRestService {

  @Inject
  private SampleService sampleService;

  @GET
  public String get() {
    return sampleService.save();
  }
}
