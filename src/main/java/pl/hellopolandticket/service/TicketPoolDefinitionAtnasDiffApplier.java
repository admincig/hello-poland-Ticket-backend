package pl.hellopolandticket.service;

import java.util.Map.Entry;
import pl.hellopoland.dto.DiscountDTO;
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
      if (remove.getTicketPoolDefinition().isNotDeletedAndHasNoAtnas()) {
        throw new ConflictingException("Bilet jest połączony z pulą");
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
    for (Entry<AvailableTicketNumberAssociation, DiscountDTO> newDiscount : diffs.toAdd) {
      for (AvailableTicketNumberAssociation child : newDiscount.getKey().getChildren()) {
        updateDiscount(child, newDiscount.getValue());
      }
      updateDiscount(newDiscount.getKey(), newDiscount.getValue());
    }

    for (Entry<AvailableTicketNumberAssociation, DiscountDTO> remove : diffs.toRemove) {
      for (AvailableTicketNumberAssociation child : remove.getKey().getChildren()) {
        child.setDiscount(null);
      }
      remove.getKey().setDiscount(null); // zadziała?
    }

    for (Entry<AvailableTicketNumberAssociation, DiscountDTO> modify : diffs.toModify) {
      for (AvailableTicketNumberAssociation child : modify.getKey().getChildren()) {
        updateDiscount(child, modify.getValue());
      }
      updateDiscount(modify.getKey(), modify.getValue());
    }
  }

  private void updateDiscount(AvailableTicketNumberAssociation atna, DiscountDTO dto) {
    Discount d = new Discount();

    boolean isHplOwner = dto.isHplOwner.booleanValue();
    d.setHplOwner(isHplOwner);

    if (isHplOwner) {
      d.setHplPart(dto.hplPart.intValue());
      d.setPartnerPart(dto.partnerPart.intValue());
    } else {
      d.setHplPart(0);
      d.setPartnerPart(dto.partnerPart.intValue());
    }

    if (dto.type.toString().equalsIgnoreCase(Discount.Type.FLAT.toString())) {
      d.setType(Discount.Type.FLAT);
      d.setValue(dto.value);
    } else {
      d.setType(Discount.Type.PERCENT);
      // percentage to fraction
      d.setValue(dto.price * (dto.value / 100));
    }

    if (Discount.Type.PERCENT.equals(d.getType())) {
      d.setPercent(dto.value);
    }

    d.setDiscountPrice(dto.price - d.getValue());

    validateDiscountValue(dto.price, d.getValue());
    validateDiscountParts(d.getHplPart(), d.getPartnerPart(), d.getValue());

    atna.setDiscount(d);
  }

  private void validateDiscountValue(int price, int discountValue) {
    if (price < discountValue) {
      throw new ConflictingException("Discount cannot be bigger than ticket price");
    }
  }

  private void validateDiscountParts(int hplPart, int partnerPart, int value) {
    if (hplPart + partnerPart != value) {
      throw new ConflictingException("Sum of discount parts is not equal to discount value");
    }
  }

}
