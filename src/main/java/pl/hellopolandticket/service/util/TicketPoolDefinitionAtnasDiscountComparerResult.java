package pl.hellopolandticket.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pl.hellopoland.dto.TicketDefinitionDTO;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

/**
 * Operates on existing atnas. Add, modifies or removes discounts on already existing atnas.
 */
public class TicketPoolDefinitionAtnasDiscountComparerResult {

  public List<Map.Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO>> toRemove =
      new ArrayList<>();
  public List<Map.Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO>> toAdd =
      new ArrayList<>();
  public List<Map.Entry<AvailableTicketNumberAssociation, TicketDefinitionDTO>> toModify =
      new ArrayList<>();
}
