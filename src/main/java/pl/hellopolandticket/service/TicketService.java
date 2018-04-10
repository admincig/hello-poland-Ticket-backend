package pl.hellopolandticket.service;

import static java.lang.Integer.valueOf;
import static pl.hellopolandticket.model.Status.PUNCHED;
import static pl.hellopolandticket.service.dto.TicketDTO.ofTicket;
import static pl.hellopolandticket.service.validator.TicketValidator.validatePunchingProperTicket;
import static pl.hellopolandticket.service.validator.TicketValidator.validateTicketHasDemandedStatus;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import pl.hellopolandticket.dao.TicketDao;
import pl.hellopolandticket.model.Ticket;
import pl.hellopolandticket.service.dto.TicketDTO;
import pl.hellopolandticket.service.exception.CannotGenerateQrCodeException;

@Stateless
@LocalBean
public class TicketService {

  private final static String TICKET_QR_CODE_HEIGHT_PROPERTY = "ticket.qrCode.height";
  private final static String TICKET_QR_CODE_WIDTH_PROPERTY = "ticket.qrCode.width";

  @Inject
  private TicketDao ticketDao;

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  public TicketDTO punchTicket(Long sightId, String serialNumber) {
    Ticket ticket = ticketDao.findBySerialNumber(serialNumber);

    validatePunchingProperTicket(sightId, ticket.getSight().getId());
    validateTicketHasDemandedStatus(ticket);

    ticket.setStatus(PUNCHED);

    return ofTicket(ticket);
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
          .encode(ticket.getSerialNumber(), BarcodeFormat.QR_CODE, width, height);

      ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

      return pngOutputStream;
    } catch (WriterException | IOException e) {
      throw new CannotGenerateQrCodeException();
    }
  }
}