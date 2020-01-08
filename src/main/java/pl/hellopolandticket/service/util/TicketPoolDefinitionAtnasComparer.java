package pl.hellopolandticket.service.util;

import java.util.List;
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

  public TicketPoolDefinitionAtnasComparerResult getTicketNumberDifferences(
      TicketPoolDefinitionDTO dto) {
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
      result.toAdd.add(new AvailableTicketNumberAssociation(td.availableTicketsNumber,
          ticketDefinition, tpd, null, null));
    }
    return result;
  }

  // TODO
  public TicketPoolDefinitionAtnasDiscountComparerResult getDiscountDifferences(
      TicketPoolDefinitionDTO dto) {
    TicketPoolDefinitionAtnasDiscountComparerResult result =
        new TicketPoolDefinitionAtnasDiscountComparerResult();

    List<AvailableTicketNumberAssociation> atnas = tpd.getUndeletedAtnas();
    outer: for (var atna : atnas) {
      for (TicketDefinitionDTO td : dto.ticketDefinitions) {
        Long ticketDefinitionId = atna.getTicketDefinition().getId();
        Integer ticketNumber = atna.getAvailableTicketsNumber();
        if (td.id.equals(ticketDefinitionId)
            && ticketNumber != null
            && td.value != null) {
          // dodanie-> bilety jeszcze nie wykupione i dto posiada zniżkę a atna nie ma zniżki
          if (ticketNumber > 0
              && (atna.getDiscount() == null
                  || (atna.getDiscount() != null && atna.getDiscount().getValue() == 0))) {
            result.toAdd.add(Map.entry(atna, td));
            continue outer;
          }
          // edycja-> bilety nie wykupione i dto posiada zniżkę inna niż atna
          else if (ticketNumber > 0
              && td.value.intValue() != atna.getDiscount().getValue()) {
            result.toModify.add(Map.entry(atna, td));
            continue outer;
          }
          // usuwanie(zakładam) -> wjb czy są już wykupione, dto.discount != null i równe zero. i
          // inne niz aktualnie
          else if (td.value.intValue() == 0
              && td.value != atna.getDiscount().getValue()) {
            result.toRemove.add(Map.entry(atna, td));
            continue outer;
          }

        }
      }
    }

    return result;
  }
}
