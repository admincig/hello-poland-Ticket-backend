package pl.hellopolandticket.service;

import java.util.Map.Entry;
import pl.hellopoland.dto.DiscountDTO;
import pl.hellopoland.dto.DiscountTypeDTO;
import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
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
      if (newDiscount.getKey().getChildren() != null) {
        for (AvailableTicketNumberAssociation child : newDiscount.getKey().getChildren()) {
          updateDiscount(child, newDiscount.getValue());
        }
      }
      updateDiscount(newDiscount.getKey(), newDiscount.getValue());
    }

    for (Entry<AvailableTicketNumberAssociation, DiscountDTO> remove : diffs.toRemove) {
      for (AvailableTicketNumberAssociation child : remove.getKey().getChildren()) {
        child.setDiscount(null);
      }
      remove.getKey().setDiscount(null);
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
    boolean isCustomComission = Boolean.TRUE.equals(dto.isCustomCommission);
    d.setCustomComission(isCustomComission);
    d.setValue(dto.value);

    TicketDefinition td = atnaService.getTD(atna.getTicketDefinition().getId());
    int originalPrice = td.getPrice();
    int amount, percent, newPrice;

    if (dto.type == DiscountTypeDTO.FLAT) {
      d.setType(Discount.Type.FLAT);
      amount = dto.value;
      percent = (int) (1.0 * amount / originalPrice * 100);
    } else {
      d.setType(Discount.Type.PERCENT);
      percent = dto.value;
      amount = (int) (1.0 * originalPrice * percent / 100);
    }
    newPrice = originalPrice - amount;
    d.setDiscountPrice(newPrice);
    d.setPercent(percent);
    d.setAmount(amount);


    if (isCustomComission) {
      d.setHplPart(dto.hplPart.intValue());
      d.setPartnerPart(dto.partnerPart.intValue());
    } else {
      d.setHplPart(0);
      d.setPartnerPart(amount);
    }

    validateDiscountValue(originalPrice, amount);
    validateDiscountParts(d.getHplPart(), d.getPartnerPart(), amount);

    atna.setDiscount(d);
  }

  private void validateDiscountValue(int price, int amount) {
    if (amount < 0) {
      throw new ConflictingException("Discount cannot be negative");
    }
    if (price < amount) {
      throw new ConflictingException("Discount cannot be bigger than ticket price");
    }
  }

  private void validateDiscountParts(int hplPart, int partnerPart, int amount) {
    if (hplPart + partnerPart != amount) {
      throw new ConflictingException("Sum of discount parts is not equal to discount amount");
    }
  }

}
