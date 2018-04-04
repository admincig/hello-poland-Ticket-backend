package pl.hellopolandticket.rest;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static pl.hellopolandticket.model.Ticket.Status.BOUGHT;
import static pl.hellopolandticket.model.Ticket.Status.INVALID;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.exception.ResourceNotFoundException;
import pl.hellopolandticket.service.exception.TicketNotBookedException;

public class TicketRestServiceTest extends BaseTest {

  @Inject
  private TicketRestService ticketRestService;

  @Inject
  private TicketDao ticketDao;

  @UsingDataSet("datasets/import.yml")
  @Test
  public void shouldBuyTickets() {
    List<Long> ticketsToBuy = asList(1L, 2L);
    Response response = ticketRestService.buyTicket(ticketsToBuy);
    List<TicketDTO> tickets = cast(response.getEntity());

    tickets.forEach(ticket -> {
      assertEquals(BOUGHT, ticket.getStatus());
      assertNotNull(ticket.getSerialNumber());
    });
  }

  @UsingDataSet("datasets/import.yml")
  @Test(expected = TicketNotBookedException.class)
  public void shouldThrowTicketNotBookedException() {
    Ticket ticket = ticketDao.findByIdsIn(singletonList(1L)).get(0);
    ticket.setStatus(INVALID);
    ticketDao.persist(singletonList(ticket));

    ticketRestService.buyTicket(singletonList(1L));
  }

  @UsingDataSet("datasets/import.yml")
  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundException() {
    ticketRestService.buyTicket(singletonList(200L));
  }
}
