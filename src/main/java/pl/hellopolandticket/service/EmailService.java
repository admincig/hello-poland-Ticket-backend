package pl.hellopolandticket.service;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.stream.Collectors.toList;
import static javax.mail.Message.RecipientType.TO;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.EmailTemplateDao;
import pl.hellopolandticket.model.EmailTemplate;
import pl.hellopolandticket.service.dto.TicketDTO;

@Slf4j
@RequestScoped
public class EmailService extends ServiceSuperclass {

  private static final String MAIL_USERNAME_PROPERTY = "mail.username";
  private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
  private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
  private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
  private static final String MAIL_SMTP_AUTH_PROPERTY = "mail.smtp.auth";
  private static final String MAIL_SMTP_STARTTLS_ENABLE_PROPERTY = "mail.smtp.starttls.enable";
  private static final String MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY = "mail.smtp.socketFactory.class";

  @Inject
  private ApplicationPropertyService applicationPropertyService;

  @Inject
  private EmailTemplateDao emailTemplateDao;

  public void sendEmailWithQrCodes(String username, String email, List<TicketDTO> tickets)
      throws MessagingException, IOException, TemplateException {
    String messageFrom = applicationPropertyService.findByName(MAIL_USERNAME_PROPERTY)
        .getPropertyValue();

    EmailTemplate emailTemplate = emailTemplateDao.findByName("ticketQrCodeEmailTemplate");

    Session session = createSessionForEmail();

    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(messageFrom));
    message.setRecipients(TO, new InternetAddress[]{new InternetAddress(email)});
    message.setSubject(emailTemplate.getSubject(), "UTF-8");
    message.setContent(createEmailContent(username, emailTemplate, tickets));

    Transport.send(message);
  }

  private Session createSessionForEmail() {
    String username = applicationPropertyService.findByName(MAIL_USERNAME_PROPERTY)
        .getPropertyValue();
    String password = applicationPropertyService.findByName(MAIL_PASSWORD_PROPERTY)
        .getPropertyValue();

    return Session.getInstance(createSessionProperties(),
        createSessionAuthenticator(username, password));
  }

  private Properties createSessionProperties() {
    Properties properties = new Properties();

    properties.put(MAIL_SMTP_HOST_PROPERTY,
        applicationPropertyService.findByName(MAIL_SMTP_HOST_PROPERTY).getPropertyValue());

    properties.put(MAIL_SMTP_PORT_PROPERTY,
        applicationPropertyService.findByName(MAIL_SMTP_PORT_PROPERTY).getPropertyValue());

    properties.put(MAIL_SMTP_AUTH_PROPERTY,
        applicationPropertyService.findByName(MAIL_SMTP_AUTH_PROPERTY).getPropertyValue());

    applicationPropertyService.find(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY).ifPresent(
        property -> properties
            .put(MAIL_SMTP_SOCKET_FACTORY_CLASS_PROPERTY, property.getPropertyValue()));

    applicationPropertyService.find(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY)
        .ifPresent(property -> properties
            .put(MAIL_SMTP_STARTTLS_ENABLE_PROPERTY, property.getPropertyValue()));

    return properties;
  }

  private Authenticator createSessionAuthenticator(String username, String password) {
    return new Authenticator() {
      public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };
  }

  private Multipart createEmailContent(String username, EmailTemplate emailTemplate,
      List<TicketDTO> tickets)
      throws IOException, TemplateException, MessagingException {
    Multipart emailContent = new MimeMultipart("related");

    List<String> ticketCIDs = generateCIDs(tickets.size());

    String bodyContent = fillQrCodeEmailTemplateWithData(emailTemplate.getTemplate(), username,
        tickets, ticketCIDs);

    MimeBodyPart emailBody = new MimeBodyPart();
    emailBody.setContent(bodyContent, "text/html; charset=utf-8");
    emailContent.addBodyPart(emailBody);

    for (int i = 0; i < tickets.size(); i++) {
      emailContent
          .addBodyPart(createTicketQrCodeAttachment(tickets.get(i).getQrCode(), ticketCIDs.get(i)));
    }

    return emailContent;
  }

  private List<String> generateCIDs(int numberOfCIDs) {
    return IntStream.rangeClosed(1, numberOfCIDs).boxed()
        .map(integer -> "image" + integer)
        .collect(toList());
  }

  private String fillQrCodeEmailTemplateWithData(String templateHtml, String username,
      List<TicketDTO> tickets, List<String> ticketCIDs) throws IOException, TemplateException {
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);

    Template template = new Template("qrTemplate", new StringReader(templateHtml), cfg);

    Map<String, String> variablesMap = new HashMap<>();
    StringBuilder ticketQrCodes = new StringBuilder();

    for (int i = 0; i < tickets.size(); i++) {
      TicketDTO ticket = tickets.get(i);
      ticketQrCodes
          .append("<p>")
          .append(ticket.getSightEvent().getName())
          .append("</p>")
          .append("<p>")
          .append(ticket.getName())
          .append("</p>")
          .append("<p>")
          .append("Numer biletu: ")
          .append(ticket.getSerialNumber())
          .append("</p>")
          .append("<p>")
          .append("Data wydarzenia: ")
          .append(makeDateHuman(ticket.getDate()))
          .append("</p>");
      ticketQrCodes
          .append("<img style=\"margin-bottom: 200px\" src=\"cid:")
          .append(ticketCIDs.get(i))
          .append("\">");
    }
    variablesMap.put("qrCodes", ticketQrCodes.toString());

    Writer out = new StringWriter();
    template.process(variablesMap, out);

    return out.toString();
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

  private String makeDateHuman(Date date) {
    String[] daysOfWeek = new String[]{"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek",
        "Sobota", "Niedziela"};

    Calendar calendar = GregorianCalendar
        .from(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    String dayOfWeek = daysOfWeek[calendar.get(DAY_OF_WEEK)];
    String dayMonthYear =
        calendar.get(DAY_OF_MONTH) + "." + (calendar.get(MONTH) + 1) + "." + calendar.get(YEAR);
    String hour = calendar.get(HOUR) + ":" + calendar.get(MINUTE);

    return dayOfWeek + ", " + dayMonthYear + " godzina " + hour;
  }
}