package pl.hellopolandticket.service.exception;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import jakarta.enterprise.context.RequestScoped;
import pl.hellopolandticket.app.Utf8ResourceBundleControl;
import pl.hellopolandticket.service.ServiceSuperclass;

@RequestScoped
public class ExceptionMessagesService extends ServiceSuperclass {

    private static final String UNKNOWN_KEY = "UNKNOWN_ERROR";
  private ResourceBundle resourceBundle;

  public ExceptionMessagesService() {
    resourceBundle = ResourceBundle.getBundle("i18n/messages", new Locale("pl"),
        new Utf8ResourceBundleControl());
  }
  public String getMessage(String exceptionKey) {
        try {
            if (resourceBundle.containsKey(exceptionKey)) {
                return resourceBundle.getString(exceptionKey);
            }
        } catch (MissingResourceException ignored) {}
        return resourceBundle.getString(UNKNOWN_KEY);
  }

    public String getCode(String exceptionKey) {
        String codeKey = exceptionKey + ".code";
        try {
            if (resourceBundle.containsKey(codeKey)) {
                return resourceBundle.getString(codeKey);
            }
        } catch (MissingResourceException ignored) {}
        return resourceBundle.getString(UNKNOWN_KEY + ".code"); // "9999"
    }

}
