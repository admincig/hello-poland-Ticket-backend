package pl.hellopolandticket.rest;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.hellopolandticket.service.TicketService;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.dto.TicketDefinitionNumberDTO;

@Path("/tickets")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TicketRestService {

  @Inject
  private TicketService ticketService;

  @POST
  @Path("/book")
  public Response bookTicket(@Valid List<TicketDefinitionNumberDTO> ticketDefinitionNumberDTOs) {
    List<TicketDTO> ticketDTOs = ticketService.bookTickets(ticketDefinitionNumberDTOs);

    return Response.ok(ticketDTOs).build();
  }
}