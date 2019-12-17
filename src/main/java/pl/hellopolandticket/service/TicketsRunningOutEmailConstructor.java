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
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

public class TicketsRunningOutEmailConstructor {
  private String title;
  private String content;

  public TicketsRunningOutEmailConstructor(TicketPool pool) {
    setPoolTitle(pool);
    setPoolContent(pool);
  }

  public TicketsRunningOutEmailConstructor(AvailableTicketNumberAssociation atna) {
    setAtnaTitle(atna);
    setAtnaContent(atna);
  }

  public String getTitle() {
    return this.title;
  }

  public String getContent() {
    return this.content;
  }

  private void setPoolTitle(TicketPool pool) {
    String sightEventName = pool.getTicketPoolDefinition().getSightEvent().getName();
    if (pool.getTicketsLeftToBuyCount() > 0) {
      this.title = "Wyczerpują się bilety na ofertę \"" + sightEventName + "\"";
    } else {
      this.title = "Bilety na ofertę " + sightEventName + " zostały wyprzedane.";
    }
  }


  private void setAtnaTitle(AvailableTicketNumberAssociation atna) {
    String sightEventName =
        atna.getTicketPool().getTicketPoolDefinition().getSightEvent().getName();
    if (atna.getTicketsLeftToBuyCount() > 0) {
      this.title = "Wyczerpują się bilety \"" + atna.getTicketDefinition().getName() + "\" - \""
          + sightEventName + "\"";
    } else {
      this.title = "Bilety na ofertę \"" + sightEventName + "\" zostały wyprzedane.";
    }
  }


  public void setPoolContent(TicketPool pool) {
    String poolName = pool.getName();
    String startDate = pool.getStartDate().toString();
    String endDate = pool.getEndDate().toString();

    TicketPoolDefinition definition = pool.getTicketPoolDefinition();
    String sightEventName = definition.getSightEvent().getName();
    String runoutDate = new Date().toString();

    StringBuffer buff = new StringBuffer("");
    buff.append("Dzień dobry, \r\n\r\n");
    buff.append("Liczba pozostałych biletów: " + pool.getTicketsLeftToBuyCount() + " \r\n");
    buff.append("Na ofertę: " + sightEventName + " \r\n");
    buff.append("Z puli biletów: " + poolName + " \r\n");
    buff.append("Dostępną w terminie od: " + startDate + " , do: " + endDate + " \r\n");
    buff.append("Rodzaje biletów: \r\n");

    HashMap<String, Long> ticketNamesMap = ticketNamesWithQuantityBoughtFromPool(pool);
    for (Map.Entry<String, Long> entry : ticketNamesMap.entrySet()) {
      buff.append("     " + entry.getKey() + " - wykupiono: " + entry.getValue() + " biletów \r\n");
    }

    buff.append("Data (prawie) wyczerpania puli: " + runoutDate + " \r\n");
    this.content = buff.toString();
  }

  private void setAtnaContent(AvailableTicketNumberAssociation atna) {
    TicketPool pool = atna.getTicketPool();
    String startDate = pool.getStartDate().toString();
    String endDate = pool.getEndDate().toString();
    TicketPoolDefinition definition = pool.getTicketPoolDefinition();
    String sightEventName = definition.getSightEvent().getName();
    String runoutDate = new Date().toString();

    StringBuffer buff = new StringBuffer("");
    buff.append("Dzień dobry, \r\n\r\n");
    buff.append("Liczba pozostałych biletów: " + atna.getTicketsLeftToBuyCount() + " \r\n");
    buff.append("Na ofertę: " + atna.getTicketDefinition().getName() + "\" "
        + sightEventName + "\"" + "\r\n");
    buff.append("Dostępną w terminie od: " + startDate + " , do: " + endDate + " \r\n");
    buff.append("Data (prawie) wyczerpania puli: " + runoutDate + " \r\n");
    this.content = buff.toString();
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
