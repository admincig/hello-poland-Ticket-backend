package pl.hellopolandticket.service.util;

import java.util.List;
import java.util.Map;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopoland.dto.TicketPoolDefinitionDTO;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.model.util.Discount;

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

  public TicketPoolDefinitionAtnasDiscountComparerResult getDiscountDifferences(
      TicketPoolDefinitionDTO dto) {
    TicketPoolDefinitionAtnasDiscountComparerResult result =
        new TicketPoolDefinitionAtnasDiscountComparerResult();

    List<AvailableTicketNumberAssociation> atnas = tpd.getUndeletedAtnas();
    outer: for (var atna : atnas) {
      for (TicketDefinitionDTO td : dto.ticketDefinitions) {
        Long ticketDefinitionId = atna.getTicketDefinition().getId();
        Integer ticketQuantity = atna.getAvailableTicketsNumber();

        if (td.id.equals(ticketDefinitionId)
            && validateDiscountCreation(td)) {

          if (validateDiscountAddition(ticketQuantity, atna.getDiscount(), td)) {
            result.toAdd.add(Map.entry(atna, td));
            continue outer;
          } else if (validateDiscountModification(ticketQuantity, atna.getDiscount(), td)) {
            result.toModify.add(Map.entry(atna, td));
            continue outer;
          } else if (validateDiscountDeletion(atna.getDiscount(), td)) {
            result.toRemove.add(Map.entry(atna, td));
            continue outer;
          }
        }
      }
    }

    return result;
  }

  private boolean validateDiscountDeletion(Discount current,
      TicketDefinitionDTO newDiscount) {
    return newDiscount.discountValue.intValue() == 0
        && newDiscount.discountValue != current.getValue();
  }

  private boolean validateDiscountModification(Integer ticketQuantity, Discount current,
      TicketDefinitionDTO newDiscount) {
    return ticketQuantity != null
        && ticketQuantity != 0
        && newDiscount.discountValue.intValue() != current.getValue();
  }

  private boolean validateDiscountAddition(Integer ticketQuantity, Discount current,
      TicketDefinitionDTO newDiscount) {
    return ticketQuantity != null
        && ticketQuantity != 0
        && (current == null
            ||
            (current != null && current.getValue() == 0));
  }

  private boolean validateDiscountCreation(TicketDefinitionDTO td) {
    return td.discountValue != null
        && td.commission != null
        && td.discountIsHplOwner != null;
  }
}
