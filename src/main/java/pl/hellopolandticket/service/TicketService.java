package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static java.util.stream.Collectors.toList;
import static pl.hellopolandticket.model.Ticket.Status.BOOKED;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.dao.TicketDefinitionDao;
import pl.hellopolandticket.model.Sight;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.model.TicketDefinition;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.dto.TicketDefinitionNumberDTO;
import pl.hellopolandticket.service.exception.CannotGenerateQrCodeException;

@Stateless
@LocalBean
public class TicketService {

  private final static String TICKET_QR_CODE_HEIGHT_PROPERTY = "ticket.qrCode.height";
  private final static String TICKET_QR_CODE_WIDTH_PROPERTY = "ticket.qrCode.width";

  @Inject
  private TicketDao ticketDao;

  @Inject
  private TicketDefinitionDao ticketDefinitionDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  public synchronized List<TicketDTO> bookTickets(
      List<TicketDefinitionNumberDTO> ticketDefinitionNumberDTOs) {

    List<Ticket> bookedTickets = new ArrayList<>();

    for (TicketDefinitionNumberDTO ticketDefinitionNumberDTO : ticketDefinitionNumberDTOs) {
      TicketDefinition ticketDefinition = ticketDefinitionDao
          .findById(ticketDefinitionNumberDTO.getTicketDefinitionId());

      Sight sight = ticketDefinition.getSight();

      for (int i = 0; i < ticketDefinitionNumberDTO.getNumberOfTickets(); i++) {
        Ticket ticket = Ticket.builder()
            .sight(sight)
            .name(ticketDefinition.getName())
            .price(ticketDefinition.getPrice())
            .date(new Date())
            .status(BOOKED)
            .build();

        bookedTickets.add(ticket);
      }

      sight.decreaseAvailableTicketsNumber(
          ticketDefinitionNumberDTO.getNumberOfTickets().intValue());
    }

    ticketDao.persist(bookedTickets);

    return bookedTickets.stream()
        .map(TicketDTO::ofTicket)
        .collect(toList());
  }

  public ByteArrayOutputStream encodeSerialNumberAsQrCode(Long ticketId) {
    try {
      int height = valueOf(
          applicationPropertyService.findByName(TICKET_QR_CODE_HEIGHT_PROPERTY).getPropertyValue());
      int width = valueOf(
          applicationPropertyService.findByName(TICKET_QR_CODE_WIDTH_PROPERTY).getPropertyValue());

      Ticket ticket = ticketDao.findById(ticketId);
      QRCodeWriter qrCodeWriter = new QRCodeWriter();
      BitMatrix bitMatrix = qrCodeWriter
          .encode(ticket.getSerialNumber().toString(), BarcodeFormat.QR_CODE, width, height);

      ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

      return pngOutputStream;
    } catch (WriterException | IOException e) {
      throw new CannotGenerateQrCodeException();
    }
  }
}