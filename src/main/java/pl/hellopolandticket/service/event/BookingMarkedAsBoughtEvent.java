package pl.hellopolandticket.service.event;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopoland.dto.booking.TicketDTO;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingMarkedAsBoughtEvent implements Serializable {

  private static final long serialVersionUID = 6300346760308664459L;

  private String paymentId;
  private String customerName;
  private String buyerNotes;
  private String recipientEmail;
  private List<TicketDTO> tickets;
  private String hash;
  private String currency;
  private Set<String> sightEventPdfAttachmentsPaths;
  private String replyToEmail;
  private Set<String> bccEmails;
  private boolean invoice;

}
