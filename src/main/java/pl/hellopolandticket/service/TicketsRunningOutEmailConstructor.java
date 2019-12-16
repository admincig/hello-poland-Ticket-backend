package pl.hellopolandticket.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import pl.hellopolandticket.model.ticket.market.Status;
import pl.hellopolandticket.model.ticket.market.Ticket;
import pl.hellopolandticket.model.ticket.partner.TicketPool;
import pl.hellopolandticket.model.ticket.partner.TicketPoolDefinition;

public class TicketsRunningOutEmailConstructor {

  private TicketPool ticketPool;

  public TicketsRunningOutEmailConstructor(TicketPool pool) {
    this.ticketPool = pool;
  }

  public String getTitle() {
    String sightEventName = ticketPool.getTicketPoolDefinition().getSightEvent().getName();
    if (ticketPool.getTicketsLeftToBuyCount() > 0) {
      return "Wyczerpują się bilety na ofertę \"" + sightEventName + "\"";
    }
    return "Bilety na ofertę " + sightEventName + " zostały wyprzedane.";
  }

  public String getContent() {
    String poolName = ticketPool.getName();
    String startDate = ticketPool.getStartDate().toString();
    String endDate = ticketPool.getEndDate().toString();

    TicketPoolDefinition definition = ticketPool.getTicketPoolDefinition();
    String sightEventName = definition.getSightEvent().getName();
    String runoutDate = new Date().toString();

    StringBuffer buff = new StringBuffer("");
    buff.append("Dzień dobry, \r\n\r\n");
    buff.append("Liczba pozostałych biletów: " + ticketPool.getTicketsLeftToBuyCount() + " \r\n");
    buff.append("Na ofertę: " + sightEventName + " \r\n");
    buff.append("Z puli biletów: " + poolName + " \r\n");
    buff.append("Dostępną w terminie od: " + startDate + " , do: " + endDate + " \r\n");
    buff.append("Rodzaje biletów: \r\n");

    HashMap<String, Long> ticketNamesMap = ticketNamesWithQuantityBoughtFromPool(ticketPool);
    for (Map.Entry<String, Long> entry : ticketNamesMap.entrySet()) {
      buff.append("     " + entry.getKey() + " - wykupiono: " + entry.getValue() + " biletów \r\n");
    }

    buff.append("Data (prawie) wyczerpania puli: " + runoutDate + " \r\n");
    return buff.toString();
  }

  private HashMap<String, Long> ticketNamesWithQuantityBoughtFromPool(TicketPool ticketPool) {
    Set<String> ticketNames =
        ticketPool.getTicketPoolDefinition().getTicketDefinitions().stream()
            .map(td -> td.getName()).collect(Collectors.toSet());

    List<Ticket> list = ticketPool.getTickets();
    HashMap<String, Long> map = new HashMap<>();
    for (String ticketName : ticketNames) {
      map.put(ticketName, countBoughtTicketsWithName(list, ticketName));
    }
    return map;
  }

  private Long countBoughtTicketsWithName(List<Ticket> list, String name) {
    return list.stream()
        .filter(ticket -> ticket.getName().equals(name))
        .filter(ticket -> Status.BOUGHT.equals(ticket.getStatus()))
        .count();
  }

}
