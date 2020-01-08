package pl.hellopolandticket.service;

import java.util.Map.Entry;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.model.util.Discount;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.util.TicketPoolDefinitionAtnasComparerResult;
import pl.hellopolandticket.service.util.TicketPoolDefinitionAtnasDiscountComparerResult;

public class TicketPoolDefinitionAtnasDiffApplier {

  private AvailableTicketNumberAssociationService atnaService;

  public TicketPoolDefinitionAtnasDiffApplier(AvailableTicketNumberAssociationService service) {
    this.atnaService = service;
  }

  // nie wiem czy zmieniać nazwę tej metody, nie mam pomysłu na nazwę
  public void apply(TicketPoolDefinitionAtnasComparerResult diffs) {
    for (AvailableTicketNumberAssociation newAtna : diffs.toAdd) {
      atnaService.add(newAtna);
    }

    for (AvailableTicketNumberAssociation remove : diffs.toRemove) {
      remove.getChildren().forEach(child -> {
        child.setAvailableTicketsNumber(0);
        child.setDeleted(true);
      });
      remove.setAvailableTicketsNumber(0);
      remove.setDeleted(true);
      if (remove.getTicketPoolDefinition().getAtnas().stream().allMatch(atna -> atna.isDeleted())) {
        throw new ConflictingException("Pula musi mieć dowiązanie przynajmniej do jednego biletu");
      }
    }

    for (Entry<AvailableTicketNumberAssociation, Integer> modify : diffs.toModify) {
      int oldAvailableTicketsNumber = modify.getKey().getAvailableTicketsNumber();
      for (AvailableTicketNumberAssociation child : modify.getKey().getChildren()) {
        if (modify.getValue() == -1) {
          child.setAvailableTicketsNumber(-1);
        } else {
          int booked = oldAvailableTicketsNumber - child.getAvailableTicketsNumber();
          child.setAvailableTicketsNumber(Math.max(0, modify.getValue() - booked));
        }
      }
      modify.getKey().setAvailableTicketsNumber(modify.getValue());
    }
  }

  public void applyDiscountUpdate(TicketPoolDefinitionAtnasDiscountComparerResult diffs) {
    for (Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO> newDiscount : diffs.toAdd) {
      updateDiscount(newDiscount.getKey(), newDiscount.getValue());
    }

    for (Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO> remove : diffs.toRemove) {
      remove.getKey().setDiscount(null); // zadziała?
    }

    for (Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO> modify : diffs.toModify) {

    }
  }

  private void updateDiscount(AvailableTicketNumberAssociation atna, TicketDefinitionDTO dto) {
    Discount d = new Discount();

    d.setHplOwner(dto.discountIsHplOwner.booleanValue());

    if (atna.getDiscount().isHplOwner()) {

      d.setHplPart(dto.discountHplPart.intValue());
      d.setPartnerPart(dto.discountPartnerPart.intValue());

      // wyliczenia dla
      // value, discountType
    } else {
      d.setHplPart(0);
      d.setPartnerPart(dto.discountPartnerPart.intValue());
      // wyliczenia dla
      // value, discountType

    }

    atna.setDiscount(d);
  }

}
