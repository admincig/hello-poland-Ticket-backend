package pl.hellopolandticket.service;

import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.TO;
import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;
import javax.activation.DataHandler;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.dao.EmailTemplateDao;
import pl.hellopolandticket.model.config.EmailTemplate;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;
import pl.hellopolandticket.service.util.EmailSendingReport;

@RequestScoped
public class EmailService extends ServiceSuperclass {
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

  @RolesAllowed({ROLE_EXTERNAL_USER})
  public EmailSendingReport sendSimpleEmail(String recipientEmail, String subject, String msg)
      throws MessagingException, UnsupportedEncodingException {
    logger.log(Level.INFO, "........... Start sending email: subject: " + subject + " to: "
        + recipientEmail + " ..............");
    var session = createSessionForEmail();
    var message = new MimeMessage(session);
    var report = new EmailSendingReport();
    try {
      message
          .setFrom(new InternetAddress(System.getProperty(MAIL_USERNAME_PROPERTY), MAIL_PERSONAL));
      message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
      message.setSubject(subject, "UTF-8");
      var mimeBodyPart = new MimeBodyPart();
      mimeBodyPart.setText(msg, "UTF-8");
      var multipart = new MimeMultipart();
      multipart.addBodyPart(mimeBodyPart);
      message.setContent(multipart);
      SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
      transport.connect();
      transport.setReportSuccess(true);
      transport.sendMessage(message, message.getAllRecipients());
      logger.log(Level.INFO, "........... End sending email: subject: " + subject + " to: "
          + recipientEmail + " ..............");
    } catch (SMTPSendFailedException e) {
      // Message has been sent.
      logger.log(Level.INFO, e.getReturnCode());
      logger.log(Level.INFO, e.getLocalizedMessage());
      for (Address addr : e.getValidSentAddresses()) {
        logger.log(Level.INFO, "Email has been sent to " + addr);
        report.validSentAddresses = e.getValidSentAddresses();
      }
      for (Address addr : e.getValidUnsentAddresses()) {
        logger.log(Level.INFO, "Email has not been sent to" + addr);
        report.validUnsentAddresses = e.getValidUnsentAddresses();
      }
      for (Address addr : e.getInvalidAddresses()) {
        logger.log(Level.INFO, "Email has not been sent to  " + addr);
        report.invalidAddresses = e.getInvalidAddresses();
      }
    } catch (MessagingException | UnsupportedEncodingException e) {
      logger.log(Level.ERROR, e.getLocalizedMessage());
      throw e;
    }
    return report;
  }

  @RolesAllowed({ROLE_EXTERNAL_USER, ROLE_ADMIN})
  public EmailSendingReport sendEmailWithQrCodes(
      BookingMarkedAsBoughtEvent bookingMarkedAsBoughtEvent)
      throws MessagingException, IOException, TemplateException {
    logger.log(Level.INFO, "........... Start sending email with qrCodes ..............");
    String recipientEmail = bookingMarkedAsBoughtEvent.getRecipientEmail();
    String replyToEmail = bookingMarkedAsBoughtEvent.getReplyToEmail();
    Set<String> bccEmails = bookingMarkedAsBoughtEvent.getBccEmails();
    var report = new EmailSendingReport();
    try {
      EmailTemplate emailTemplate = emailTemplateDao.findByName("ticketQrCodeEmailTemplate");
      Session session = createSessionForEmail();
      MimeMessage message = new MimeMessage(session);
      message
          .setFrom(new InternetAddress(System.getProperty(MAIL_USERNAME_PROPERTY), MAIL_PERSONAL));
      message.setRecipients(TO, new InternetAddress[] {new InternetAddress(recipientEmail)});
      if (replyToEmail != null) {
        message.setReplyTo(new InternetAddress[] {new InternetAddress(replyToEmail)});
      }
      if (bccEmails != null) {
        Set<InternetAddress> addresses = new HashSet<>();
        for (String email : bccEmails) {
          addresses.add(new InternetAddress(email));
        }
        message.setRecipients(BCC, addresses.toArray(new InternetAddress[bccEmails.size()]));
      }
      message.setSubject(emailTemplate.getSubject(), "UTF-8");
      message.setContent(
          createEmailContent(bookingMarkedAsBoughtEvent.getCustomerName(), emailTemplate,
              bookingMarkedAsBoughtEvent.getTickets(), bookingMarkedAsBoughtEvent.getP24OrderId(),
              bookingMarkedAsBoughtEvent.getSightEventPdfAttachmentsPaths()));
      SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
      transport.connect();
      transport.setReportSuccess(true);
      transport.sendMessage(message, message.getAllRecipients());
    } catch (SMTPSendFailedException e) {
      // Message has been sent.
      logger.log(Level.INFO, e.getReturnCode());
      logger.log(Level.INFO, e.getLocalizedMessage());
      for (Address addr : e.getValidSentAddresses()) {
        logger.log(Level.INFO, "Email has been sent to " + addr);
        report.validSentAddresses = e.getValidSentAddresses();
      }
      for (Address addr : e.getValidUnsentAddresses()) {
        logger.log(Level.INFO, "Email has not been sent to" + addr);
        report.validUnsentAddresses = e.getValidUnsentAddresses();
      }
      for (Address addr : e.getInvalidAddresses()) {
        logger.log(Level.INFO, "Email has not been sent to " + addr);
        report.invalidAddresses = e.getInvalidAddresses();
      }
    } catch (Exception e) {
      logger.log(Level.ERROR, e.getLocalizedMessage());
      throw e;
    }
    logger.log(Level.INFO, "........... End sending email with qrCodes ..............");
    return report;
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
