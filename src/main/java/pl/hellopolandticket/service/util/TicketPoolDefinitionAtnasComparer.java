package pl.hellopolandticket.service.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    List<AvailableTicketNumberAssociation> atnas = tpd.getUndeletedAtnas();
    outer: for (var atna : atnas) {
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
      for (var atna : atnas) {
        Long ticketDefinitionId = atna.getTicketDefinition().getId();
        if (td.id.equals(ticketDefinitionId)) {
          continue outer;
        }
      }
      TicketDefinition ticketDefinition = new TicketDefinition();
      ticketDefinition.setId(td.id);
      AvailableTicketNumberAssociation parentAtna =
          new AvailableTicketNumberAssociation(td.availableTicketsNumber,
              ticketDefinition, tpd, null, null);
      result.toAdd.add(parentAtna);
      result.toAdd.addAll(tpd.getTicketPools().stream()
          .map(tp -> new AvailableTicketNumberAssociation(td.availableTicketsNumber,
              ticketDefinition, null, tp, parentAtna))
          .collect(Collectors.toList()));
    }
    return result;
  }
}
