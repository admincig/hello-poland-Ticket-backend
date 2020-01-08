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
      // nowe wartosci discount
      var dto = newDiscount.getValue();
      Discount d = new Discount();
      d.setValue(dto.value.intValue());
      d.setHplOwner(dto.isHplOwner);
      d.setHplPart(dto.hplPart.intValue());
      d.setPartnerPart(dto.partnerPart.intValue());

      // inaczej i ładniej
      if (dto.type.name().equalsIgnoreCase(Discount.Type.FLAT.toString())) {
        d.setType(Discount.Type.FLAT);
      } else if (dto.type.name().equalsIgnoreCase(Discount.Type.PERCENT.toString())) {
        d.setType(Discount.Type.PERCENT);
      }

      // ustawiam nowy discount na atnie
      newDiscount.getKey().setDiscount(d);
    }

    for (Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO> remove : diffs.toRemove) {

    }

    for (Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO> modify : diffs.toModify) {

    }
  }
}
