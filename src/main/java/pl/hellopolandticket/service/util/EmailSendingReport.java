package pl.hellopolandticket.service.util;

import jakarta.mail.Address;

public class EmailSendingReport {
  public Address[] validSentAddresses;
  public Address[] validUnsentAddresses;
  public Address[] invalidAddresses;
}
