package pl.hellopolandticket.service.util;

import java.util.Map;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

public class TicketPoolDefinitionAtnasComparer {

  private TicketPoolDefinition tpd;

  public TicketPoolDefinitionAtnasComparer(TicketPoolDefinition tpd) {
    this.tpd = tpd;
  }

  public TicketPoolDefinitionAtnasComparerResult getDifferences(TicketPoolDefinitionDTO dto) {
    TicketPoolDefinitionAtnasComparerResult result = new TicketPoolDefinitionAtnasComparerResult();

    outer: for (var atna : tpd.getAtnas()) {
      Long ticketDefinitionId = atna.getTicketDefinition().getId();
      for (TicketDefinitionDTO td : dto.ticketDefinitions) {
        if (td.id.equals(ticketDefinitionId)) {
          if (td.availableTicketsNumber != atna.getAvailableTicketsNumber()) {
            result.toModify.add(Map.entry(atna, td.availableTicketsNumber));
          }
          continue outer;
        }
      }
      result.toRemove.add(atna);
    }
    outer: for (var td : dto.ticketDefinitions) {
      for (var atna : tpd.getAtnas()) {
        Long ticketDefinitionId = atna.getTicketDefinition().getId();
        if (td.id.equals(ticketDefinitionId)) {
          continue outer;
        }
      }
      TicketDefinition ticketDefinition = new TicketDefinition();
      ticketDefinition.setId(td.id);
      result.toAdd.add(new AvailableTicketNumberAssociation(td.availableTicketsNumber,
          ticketDefinition, tpd, null, null));
    }
    return result;
  }
}
