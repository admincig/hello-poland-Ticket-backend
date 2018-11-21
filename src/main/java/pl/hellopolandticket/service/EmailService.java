package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.activation.DataHandler;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.dao.EmailTemplateDao;
import pl.hellopolandticket.model.config.EmailTemplate;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;

@RequestScoped
public class EmailService extends ServiceSuperclass {
  private static final Logger HELPDESK_lOG = System.getLogger("helpdesk-orders");

  private static final String MAIL_TICKET_COPY = "ticket.copy@hello-poland.pl";
  private static final String MAIL_PERSONAL = "Bilety Hello Poland";
  private static final String MAIL_USERNAME_PROPERTY = "mail.username";
  private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
  private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
  private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
  private static final String MAIL_SMTP_AUTH_PROPERTY = "mail.smtp.auth";
  private static final String MAIL_SMTP_STARTTLS_ENABLE_PROPERTY = "mail.smtp.starttls.enable";
  private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY =
      "mail.smtp.socketFactory.class";

  @Inject
  private EmailTemplateDao emailTemplateDao;

  @Inject
  private TicketService ticketService;

  public void sendEmailWithQrCodes(BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent)
      throws MessagingException, IOException, TemplateException {
    try {
      EmailTemplate emailTemplate = emailTemplateDao.findByName("ticketQrCodeEmailTemplate");
      Session session = createSessionForEmail();
      MimeMessage message = new MimeMessage(session);
      message
          .setFrom(new InternetAddress(System.getProperty(MAIL_USERNAME_PROPERTY), MAIL_PERSONAL));
      message.setRecipients(BCC, new InternetAddress[] {new InternetAddress(MAIL_TICKET_COPY)});
      message.setRecipients(TO, new InternetAddress[] {
          new InternetAddress(bookingMarkedAsBoughtEvent.getCustomerEmail())});
      message.setSubject(emailTemplate.getSubject(), "UTF-8");
      message.setContent(
          createEmailContent(bookingMarkedAsBoughtEvent.getCustomerName(), emailTemplate,
              bookingMarkedAsBoughtEvent.getTickets(), bookingMarkedAsBoughtEvent.getP24OrderId(),
              bookingMarkedAsBoughtEvent.getSightEventPdfAttachmentsPaths()));
      Transport.send(message);
      HELPDESK_lOG.log(Level.INFO, getHelpdeskLogMessage(bookingMarkedAsBoughtEvent, true));
    } catch (MessagingException | IOException | TemplateException e) {
      HELPDESK_lOG.log(Level.INFO, getHelpdeskLogMessage(bookingMarkedAsBoughtEvent, false));
      throw e;
    }
  }

  private String getHelpdeskLogMessage(BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent,
      boolean mailWasSend) throws MessagingException {
    StringBuilder sb = new StringBuilder();
    sb.append("PŁATNOŚĆ: ").append(getValueOfOrder(bookingMarkedAsBoughtEvent.getTickets()))
        .append(" " + bookingMarkedAsBoughtEvent.getP24Currency()).append(" | NR TRANSAKCJI P24: ")
        .append(bookingMarkedAsBoughtEvent.getP24OrderId()).append(" | CZY MAIL ZOSTAŁ WYSŁANY: ")
        .append(mailWasSend ? "tak" : "nie").append(" | NAZWA UŻUTKOWNIKA: ")
        .append(bookingMarkedAsBoughtEvent.getCustomerName()).append(" | ADRES EMAIL: ")
        .append(bookingMarkedAsBoughtEvent.getCustomerEmail());
    return sb.toString();
  }

  private double getValueOfOrder(List<TicketDTO> tickets) {
    return tickets.stream().collect(Collectors.summingDouble(t -> Double.valueOf(t.price) / 100));
  }

  private Session createSessionForEmail() {
    return Session.getInstance(createSessionProperties(), createSessionAuthenticator(
        System.getProperty(MAIL_USERNAME_PROPERTY), System.getProperty(MAIL_PASSWORD_PROPERTY)));
  }

  private Properties createSessionProperties() {
    Properties properties = new Properties();
    properties.put(MAIL_SMTP_HOST_PROPERTY, System.getProperty(MAIL_SMTP_HOST_PROPERTY));
    properties.put(MAIL_SMTP_PORT_PROPERTY, System.getProperty(MAIL_SMTP_PORT_PROPERTY));
    properties.put(MAIL_SMTP_AUTH_PROPERTY, System.getProperty(MAIL_SMTP_AUTH_PROPERTY));
    if (System.getProperty(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY) != null) {
      properties.put(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY,
          System.getProperty(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY));
    }
    if (System.getProperty(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY) != null) {
      properties.put(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY,
          System.getProperty(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY));
    }
    return properties;
  }

  private Authenticator createSessionAuthenticator(String username, String password) {
    return new Authenticator() {
      @Override
      public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };
  }

  private Multipart createEmailContent(String username, EmailTemplate emailTemplate,
      List<TicketDTO> tickets, String p24OrderId, Set<String> sightEventPdfAttachmentsPaths)
      throws IOException, TemplateException, MessagingException {

    Multipart emailContent = new MimeMultipart("related");
    List<String> ticketCIDs = generateCIDs(tickets.size());
    String bodyContent = fillQrCodeEmailTemplateWithData(emailTemplate.getTemplate(), username,
        tickets, ticketCIDs, p24OrderId);
    MimeBodyPart emailBody = new MimeBodyPart();
    emailBody.setContent(bodyContent, "text/html; charset=utf-8");
    emailContent.addBodyPart(emailBody);

    for (int i = 0; i < tickets.size(); i++) {
      emailContent
          .addBodyPart(createTicketQrCodeAttachment(tickets.get(i).qrCode, ticketCIDs.get(i)));
    }
    for (String pdfPath : sightEventPdfAttachmentsPaths) {
      emailContent.addBodyPart(attachFile(pdfPath));
    }

    return emailContent;
  }

  private List<String> generateCIDs(int numberOfCIDs) {
    return IntStream.rangeClosed(1, numberOfCIDs).boxed().map(integer -> "image" + integer)
        .collect(toList());
  }

  private String fillQrCodeEmailTemplateWithData(String templateHtml, String username,
      List<TicketDTO> tickets, List<String> ticketCIDs, String p24OrderId)
      throws IOException, TemplateException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    Template template = new Template("qrTemplate", new StringReader(templateHtml), cfg);
    Map<String, String> variablesMap = new HashMap<>();
    variablesMap.put("userName", username);
    StringBuilder ticketQrCodes = new StringBuilder();
    for (int i = 0; i < tickets.size(); i++) {
      TicketDTO ticket = tickets.get(i);
      EmailTemplate ticketTemplate = emailTemplateDao.findByName("ticketQrCodeTemplate");
      String ticketQR =
          fillTicketQrCodeTemplate(ticketTemplate, ticket, ticketCIDs.get(i), p24OrderId, cfg);
      ticketQrCodes.append("<p>").append(ticketQR).append("</p>");
    }
    variablesMap.put("qrCodes", ticketQrCodes.toString());
    Writer out = new StringWriter();
    template.process(variablesMap, out);
    return out.toString();
  }

  private String fillTicketQrCodeTemplate(EmailTemplate ticketTemplate, TicketDTO ticket,
      String ticketCID, String p24OrderId, Configuration cfg)
      throws IOException, TemplateException {
    Template template =
        new Template("ticketQRTemplate", new StringReader(ticketTemplate.getTemplate()), cfg);
    Map<String, String> variablesMap = new HashMap<>();
    variablesMap.put("P24_transactionNumber", p24OrderId);
    variablesMap.put("sightEventName", getSigthEventName(ticket));
    variablesMap.put("sightEventDate", makeDateHuman(ticket.date));
    variablesMap.put("qrCode", "<img src=\"cid:" + ticketCID + "\">");
    variablesMap.put("ticketName", ticket.name);
    variablesMap.put("ticketNumber", ticket.serialNumber);
    variablesMap.put("ticketPrice", getHumanReadablePrice(ticket.price));
    Writer out = new StringWriter();
    template.process(variablesMap, out);
    return out.toString();
  }

  private String getSigthEventName(TicketDTO ticket) {
    SightEvent sightEvent = ticketService.findSightEventForTicket(ticket.id);
    return sightEvent.getName();
  }

  private String getHumanReadablePrice(Integer price) {
    var p = Float.valueOf(price.toString()) / 100;
    return String.format("%.2f", p) + " PLN";
  }

  private MimeBodyPart createTicketQrCodeAttachment(ByteArrayOutputStream image, String cid)
      throws MessagingException {
    MimeBodyPart imagePart = new MimeBodyPart();
    imagePart
        .setDataHandler(new DataHandler(new ByteArrayDataSource(image.toByteArray(), "image/png")));
    imagePart.setFileName(cid + ".png");
    imagePart.setContentID("<" + cid + ">");
    imagePart.setDisposition(MimeBodyPart.INLINE);

    return imagePart;
  }

  private MimeBodyPart attachFile(String filePath) throws MessagingException, IOException {
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(filePath);
    return attachmentPart;
  }

  private String makeDateHuman(Date date) {
    LocalDateTime ldt = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    String dayOfWeek = ldt.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pl", "PL"));
    String dayOfMonth =
        String.valueOf(ldt.getDayOfMonth() < 10 ? "0" + ldt.getDayOfMonth() : ldt.getDayOfMonth());
    String month =
        String.valueOf(ldt.getMonthValue() < 10 ? "0" + ldt.getMonthValue() : ldt.getMonthValue());
    String year = String.valueOf(ldt.getYear());
    String hour = String.valueOf(ldt.getHour() < 10 ? "0" + ldt.getHour() : ldt.getHour());
    String minute = String.valueOf(ldt.getMinute() < 10 ? "0" + ldt.getMinute() : ldt.getMinute());

    String dayMonthYear = dayOfMonth + "." + month + "." + year;
    String hourAndMinute = hour + ":" + minute;

    return dayOfWeek + ", " + dayMonthYear + " godzina " + hourAndMinute;
  }

}
