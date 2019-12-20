package pl.hellopolandticket.service;

import java.util.Map.Entry;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;
import pl.hellopolandticket.service.exception.conflict.ConflictingException;
import pl.hellopolandticket.service.util.TicketPoolDefinitionAtnasComparerResult;

public class TicketPoolDefinitionAtnasDiffApplier {

  private AvailableTicketNumberAssociationService atnaService;

  public TicketPoolDefinitionAtnasDiffApplier(AvailableTicketNumberAssociationService service) {
    this.atnaService = service;
  }

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
}
