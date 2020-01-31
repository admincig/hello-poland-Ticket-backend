package pl.hellopolandticket.service.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import pl.hellopoland.dto.DiscountDTO;
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
          if (!Objects.equals(td.availableTicketsNumber, atna.getAvailableTicketsNumber())) {
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
              ticketDefinition, tpd, null, null, null);
      result.toAdd.add(parentAtna);
      result.toAdd.addAll(tpd.getTicketPools().stream()
          .map(tp -> new AvailableTicketNumberAssociation(td.availableTicketsNumber,
              ticketDefinition, null, tp, parentAtna, null))
          .collect(Collectors.toList()));
    }
    return result;
  }

  public TicketPoolDefinitionAtnasDiscountComparerResult getDiscountDifferences(
      TicketPoolDefinitionDTO dto) {
    TicketPoolDefinitionAtnasDiscountComparerResult result =
        new TicketPoolDefinitionAtnasDiscountComparerResult();

    List<AvailableTicketNumberAssociation> atnas = tpd.getUndeletedAtnas();
    for (var atna : atnas) {
      for (TicketDefinitionDTO td : dto.ticketDefinitions) {
        Long ticketDefinitionId = atna.getTicketDefinition().getId();
        Integer ticketQuantity = atna.getAvailableTicketsNumber();

        if (td.id.equals(ticketDefinitionId) && validateDiscountCreation(td.discount)) {
          if (shouldDiscountBeDeleted(atna.getDiscount(), td.discount)) {
            result.toRemove.add(Map.entry(atna, new DiscountDTO()));
            break;
          } else if (shouldDiscountBeAdded(ticketQuantity, atna.getDiscount(), td.discount)) {
            result.toAdd.add(Map.entry(atna, td.discount));
            break;
          } else if (shouldDiscountBeModified(ticketQuantity, atna.getDiscount(), td.discount)) {
            result.toModify.add(Map.entry(atna, td.discount));
            break;
          }
        }
      }
    }

    return result;
  }

  private boolean shouldDiscountBeDeleted(Discount current, DiscountDTO newDiscount) {
    return newDiscount == null;
  }

  private boolean shouldDiscountBeModified(Integer ticketQuantity, Discount current,
      DiscountDTO newDiscount) {
    return ticketQuantity != null
        && ticketQuantity != 0
        && newDiscount.value.intValue() != current.getValue();
  }

  private boolean shouldDiscountBeAdded(Integer ticketQuantity, Discount current,
      DiscountDTO newDiscount) {
    return ticketQuantity != null
        && ticketQuantity != 0
        && newDiscount != null
        && newDiscount.value > 0
        && (current == null || current.getValue() == 0);
  }

  private boolean validateDiscountCreation(DiscountDTO newDiscount) {
    return newDiscount == null ||
        (newDiscount.value != null
            && newDiscount.type != null
            && validateCommissions(newDiscount));
  }

  private boolean validateCommissions(DiscountDTO newDiscount) {
    return newDiscount.isCustomCommission == null
        || !newDiscount.isCustomCommission
        || !(newDiscount.hplPart == null
            || newDiscount.partnerPart == null
            || newDiscount.hplPart < 0
            || newDiscount.partnerPart < 0);
  }
}
