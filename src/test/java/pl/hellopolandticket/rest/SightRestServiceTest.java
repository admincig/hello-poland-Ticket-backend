package pl.hellopolandticket.rest;

import static org.junit.Assert.assertEquals;
import static pl.hellopolandticket.model.Status.PUNCHED;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Test;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.exception.conflict.PunchingTicketForWrongSightException;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;

@Slf4j
@UsingDataSet("scripts/datasets/import.yml")
public class SightRestServiceTest extends BaseTest {

  @Inject
  private SightRestService sightRestService;

  @Test
  public void shouldPunchTicket() {
    Response response = sightRestService
        .punchTicket(2L, "412CBA1C1191B0C5C94142ED58B3C28BBCF7D0400A3109D09DAEE61ED3CA69F3");

    TicketDTO ticket = (TicketDTO) response.getEntity();

    assertEquals(PUNCHED, ticket.getStatus());
  }

  @Test(expected = PunchingTicketForWrongSightException.class)
  public void shouldThrowPunchedTicketForWrongSightExceptionWhenSerialNumberRegardsAnotherSight() {
    sightRestService
        .punchTicket(1L, "412CBA1C1191B0C5C94142ED58B3C28BBCF7D0400A3109D09DAEE61ED3CA69F3");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenTicketForSerialNumberNotExist() {
    sightRestService
        .punchTicket(1L, "fsgdfgh34gdfh4");
  }

  @Test(expected = WrongTicketStatusException.class)
  public void shouldThrowWrongTicketStatusExceptionWhenTicketHasAnotherStatusThanBought() {
    sightRestService
        .punchTicket(2L, "0C54D64568A3ACD560EC5591D78A6D440CF4F5CD0142C186F4AAAF7F5307DFA3");
  }
}
