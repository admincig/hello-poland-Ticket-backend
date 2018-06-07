package pl.hellopolandticket.service.exception;

import java.util.Locale;
import java.util.ResourceBundle;
import javax.enterprise.context.RequestScoped;
import javax.interceptor.Interceptors;
import pl.hellopolandticket.app.LoggingHandler;
import pl.hellopolandticket.app.Utf8ResourceBundleControl;

@RequestScoped
@Interceptors(value = LoggingHandler.class)
public class ExceptionMessagesService {

  private ResourceBundle resourceBundle;

  public ExceptionMessagesService() {
    resourceBundle = ResourceBundle
        .getBundle("i18n/messages", new Locale("pl"), new Utf8ResourceBundleControl());
  }

  public String getMessage(String exceptionSimpleClassName) {
    return resourceBundle.getString(exceptionSimpleClassName);
  }
}
