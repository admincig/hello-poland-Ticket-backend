package pl.hellopolandticket.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.eclipse.angus.mail.smtp.SMTPTransport;
import pl.hellopoland.dto.booking.TicketDTO;
import pl.hellopolandticket.dao.EmailTemplateDao;
import pl.hellopolandticket.model.config.EmailTemplate;
import pl.hellopolandticket.model.sightevent.SightEvent;
import pl.hellopolandticket.service.event.BookingMarkedAsBoughtEvent;
import pl.hellopolandticket.service.util.EmailSendingReport;

import jakarta.activation.DataHandler;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.*;
import java.lang.System.Logger.Level;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static jakarta.mail.Message.RecipientType.BCC;
import static jakarta.mail.Message.RecipientType.TO;
import static pl.hellopolandticket.model.auth.Role.ROLE_ADMIN;
import static pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER;

@RequestScoped
public class EmailSenderService extends ServiceSuperclass {
  private static final String MAIL_PERSONAL_PROPERTY = "mail.personal";
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
          .setFrom(
              new InternetAddress(System.getProperty(MAIL_USERNAME_PROPERTY), System.getProperty(
                  MAIL_PERSONAL_PROPERTY), "UTF-8"));
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
      BookingMarkedAsBoughtEvent event)
      throws MessagingException, IOException, TemplateException {
    logger.log(Level.INFO, "........... Start sending email with qrCodes ..............");
    String recipientEmail = event.getRecipientEmail();
    String replyToEmail = event.getReplyToEmail();
    Set<String> bccEmails = event.getBccEmails();
    var report = new EmailSendingReport();
    try {
      EmailTemplate emailTemplate = emailTemplateDao.findByName("ticketQrCodeEmailTemplate");
      Session session = createSessionForEmail();
      MimeMessage message = new MimeMessage(session);
      message
          .setFrom(new InternetAddress(System.getProperty(MAIL_USERNAME_PROPERTY),
              System.getProperty(MAIL_PERSONAL_PROPERTY), "UTF-8"));
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
      String subject = emailTemplate.getSubject();
      if (event.isInvoice()) {
        subject = "[Prośba o wystawienie faktury] " + subject;
      }
      message.setSubject(subject, "UTF-8");
      message.setContent(
          createEmailContent(event.getCustomerName(), event.getBuyerNotes(), emailTemplate,
              event.getTickets(), event.getPaymentId(),
              event.getSightEventPdfAttachmentsPaths()));
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

  private Multipart createEmailContent(String username, String buyerNotes, EmailTemplate emailTemplate,
      List<TicketDTO> tickets, String paymentId, Set<String> sightEventPdfAttachmentsPaths)
      throws IOException, TemplateException, MessagingException {

    Multipart emailContent = new MimeMultipart("related");
    List<String> ticketCIDs = generateCIDs(tickets.size());
    String bodyContent = fillQrCodeEmailTemplateWithData(emailTemplate.getTemplate(), username, buyerNotes,
        tickets, ticketCIDs, paymentId);
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

  private String fillQrCodeEmailTemplateWithData(String templateHtml, String username, String buyerNotes,
      List<TicketDTO> tickets, List<String> ticketCIDs, String paymentId)
      throws IOException, TemplateException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    Template template = new Template("qrTemplate", new StringReader(templateHtml), cfg);
    Map<String, String> variablesMap = new HashMap<>();
    variablesMap.put("userName", username);
    variablesMap.put("year", String.valueOf(LocalDate.now().getYear()));
    variablesMap.put("buyerNotes", StringUtils.isBlank(buyerNotes) ? "" : ("Informacja od kupującego:<br>" + buyerNotes));
    StringBuilder ticketQrCodes = new StringBuilder();
    for (int i = 0; i < tickets.size(); i++) {
      TicketDTO ticket = tickets.get(i);
      EmailTemplate ticketTemplate = emailTemplateDao.findByName("ticketQrCodeTemplate");
      String ticketQR = fillTicketQrCodeTemplate(ticketTemplate, ticket, ticketCIDs.get(i), paymentId, cfg);
      ticketQrCodes.append("<p>").append(ticketQR).append("</p>");
    }
    variablesMap.put("qrCodes", ticketQrCodes.toString());
    Writer out = new StringWriter();
    template.process(variablesMap, out);
    return out.toString();
  }

  private String fillTicketQrCodeTemplate(EmailTemplate ticketTemplate, TicketDTO ticket,
      String ticketCID, String paymentId, Configuration cfg)
      throws IOException, TemplateException {
    Template template =
        new Template("ticketQRTemplate", new StringReader(ticketTemplate.getTemplate()), cfg);
    Map<String, String> variablesMap = new HashMap<>();
    variablesMap.put("paymentId", paymentId);
    variablesMap.put("sightEventName", getSigthEventName(ticket));
    variablesMap.put("sightEventDate", makeDateHuman(ticket.date, ticket.wholeDay));
    variablesMap.put("qrCode", "<img src=\"cid:" + ticketCID + "\">");
    variablesMap.put("ticketName", ticket.name);
    variablesMap.put("ticketNumber", ticket.serialNumber);
    Integer price = ticket.price;
    if (ticket.discount != null) {
      price = ticket.discount.price;
    }
    variablesMap.put("ticketPrice", getHumanReadablePrice(price));
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

  private String makeDateHuman(Date date, boolean wholeDay) {
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

    return dayOfWeek + ", " + dayMonthYear + (wholeDay ? "" : " godzina " + hourAndMinute);
  }

}
