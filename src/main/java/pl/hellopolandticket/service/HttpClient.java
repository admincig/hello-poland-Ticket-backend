package pl.hellopolandticket.service;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.json.bind.JsonbBuilder;
import pl.hellopolandticket.service.event.HPLPushEvent;

@ApplicationScoped
public class HttpClient extends ServiceSuperclass implements Serializable {

  private static final long serialVersionUID = 7529870890163651237L;

  public void sendPostRequest(@ObservesAsync HPLPushEvent hplPushEvent) {
    try {
      HttpURLConnection httpURLConnection =
          createHttpConnectionWithPostRequestMethod(hplPushEvent.getURLPath());
      String postJsonData = JsonbBuilder.create().toJson(hplPushEvent.getPush());
      sendPostRequestWithBody(httpURLConnection, postJsonData);
      if (!isResponseCodeEqualNoContent(httpURLConnection.getResponseCode())) {
        logger.log(Level.ERROR, "Response code doesn't equal expected one. Status: {}",
            httpURLConnection.getResponseCode());
      }
    } catch (IOException e) {
      logger.log(Level.ERROR, "Sending a post request with attractions exception. {}",
          e.getMessage());
    }
  }

  private HttpURLConnection createHttpConnectionWithPostRequestMethod(String URLPath)
      throws IOException {
    URL url = new URL(URLPath);
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setRequestProperty("Content-Type", "application/json");
    httpURLConnection.setDoOutput(true);
    return httpURLConnection;
  }

  private void sendPostRequestWithBody(HttpURLConnection httpURLConnection, String body)
      throws IOException {
    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
    wr.write(body.getBytes("UTF-8"));
    wr.flush();
    wr.close();
  }

  private boolean isResponseCodeEqualNoContent(int responseCode) {
    return responseCode == HTTP_NO_CONTENT;
  }
}
