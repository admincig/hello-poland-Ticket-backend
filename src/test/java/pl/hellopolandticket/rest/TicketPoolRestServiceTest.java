package pl.hellopolandticket.rest;

import javax.inject.Inject;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.junit.Ignore;
import org.junit.Test;
import pl.hellopolandticket.BaseTest;
import pl.hellopolandticket.service.exception.conflict.PunchingTicketForWrongSightException;
import pl.hellopolandticket.service.exception.conflict.TicketInvalidException;
import pl.hellopolandticket.service.exception.conflict.WrongTicketStatusException;
import pl.hellopolandticket.service.exception.notfound.ResourceNotFoundException;

@UsingDataSet("scripts/datasets/import.yml")
@Ignore
public class TicketPoolRestServiceTest extends BaseTest {

  @Inject
  private TicketPoolRestService ticketPoolRestService;

  @Test(expected = PunchingTicketForWrongSightException.class)
  public void shouldThrowPunchedTicketForWrongSightExceptionWhenSerialNumberRegardsAnotherSight() {
    ticketPoolRestService.punchTicket(1L,
        "412CBA1C1191B0C5C94142ED58B3C28BBCF7D0400A3109D09DAEE61ED3CA69F3");
  }

  @Test(expected = ResourceNotFoundException.class)
  public void shouldThrowResourceNotFoundExceptionWhenTicketForSerialNumberNotExist() {
    ticketPoolRestService.punchTicket(1L, "fsgdfgh34gdfh4");
  }

  @Test(expected = WrongTicketStatusException.class)
  public void shouldThrowWrongTicketStatusExceptionWhenPunchingTicketHasAnotherStatusThanBought() {
    ticketPoolRestService.punchTicket(2L,
        "0C54D64568A3ACD560EC5591D78A6D440CF4F5CD0142C186F4AAAF7F5307DFA3");
  }

  @Test(expected = TicketInvalidException.class)
  public void shouldThrowTicketInvalidExceptionWhenPunchingInvalidTicket() {
    ticketPoolRestService.punchTicket(2L,
        "635735E7C7972D5B0852F51BED44463D774C3054C3563C1F6ABE5C1103A5FA8F");
  }
}
