package pl.hellopolandticket.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

/**
 * Based on incoming ticketNumber adds new atnas, removes existing or modifies it
 */
public class TicketPoolDefinitionAtnasComparerResult {

  public List<AvailableTicketNumberAssociation> toRemove = new ArrayList<>();
  public List<AvailableTicketNumberAssociation> toAdd = new ArrayList<>();
  public List<Map.Entry<AvailableTicketNumberAssociation, Integer>> toModify = new ArrayList<>();

}
