package pl.hellopolandticket.service.util;

import javax.mail.Address;

public class EmailSendingReport {
  public Address[] validSentAddresses;
  public Address[] validUnsentAddresses;
  public Address[] invalidAddresses;
}
