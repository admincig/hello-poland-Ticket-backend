package pl.hellopolandticket.service.event;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopoland.dto.booking.TicketDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingMarkedAsBoughtEvent implements Serializable {

    private static final long serialVersionUID = 6300346760308664459L;

    private String paymentId;
    private String customerName;
    private String buyerNotes;
    private String recipientEmail;
    private List<TicketDTO> tickets;
    private String hash;
    private String currency;

    // TO MUSI BYĆ (żeby działały obecne wywołania i EmailSenderService)
    private Set<String> sightEventPdfAttachmentsPaths;

    // opcjonalnie, jeśli chcesz dalej rozwijać mapę (może być, ale nie przeszkadza)
    private Map<String, String> sightEventPdfAttachments;

    private String replyToEmail;
    private Set<String> bccEmails;
    private boolean invoice;
}
