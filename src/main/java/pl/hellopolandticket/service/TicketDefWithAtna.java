package pl.hellopolandticket.service;

import pl.hellopolandticket.model.ticket.partner.TicketDefinition;
import pl.hellopolandticket.model.util.AvailableTicketNumberAssociation;

public record TicketDefWithAtna(
        TicketDefinition ticketDefinition,
        AvailableTicketNumberAssociation atna
) {}
