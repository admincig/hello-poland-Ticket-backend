package pl.hellopolandticket.model.util;

import static java.util.UUID.randomUUID;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UUIDGeneratorUtil {

  public static String generateUUID() {
    try {
      MessageDigest salt = MessageDigest.getInstance("SHA-256");
      salt.update(randomUUID().toString().getBytes("UTF-8"));
      return printHexBinary(salt.digest());
    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
      log.error("Can't generate random UUID {}", e);
      return null;
    }
  }
}
