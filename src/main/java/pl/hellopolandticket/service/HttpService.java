package pl.hellopolandticket.service;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import pl.hellopolandticket.service.exception.SendingHttpRequestException;

@Stateless
@LocalBean
public class HttpService {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public void sendPostRequestWithAttractionsToURL(String URLPath, Object... attractions) {
    try {
      HttpURLConnection httpURLConnection = createHttpConnectionWithPostRequestMethod(URLPath);

      String postJsonData = objectMapper.writeValueAsString(attractions);

      sendPostRequestWithBody(httpURLConnection, postJsonData);

      if (!isResponseCodeEqualNoContent(httpURLConnection.getResponseCode())) {
        throw new SendingHttpRequestException();
      }
    } catch (IOException e) {
      throw new SendingHttpRequestException();
    }
  }

  private HttpURLConnection createHttpConnectionWithPostRequestMethod(String URLPath)
      throws IOException {
    URL url = new URL(URLPath);

    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("POST");
    httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
    httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    httpURLConnection.setRequestProperty("Content-Type", "application/json");
    httpURLConnection.setDoOutput(true);

    return httpURLConnection;
  }

  private void sendPostRequestWithBody(HttpURLConnection httpURLConnection, String body)
      throws IOException {
    DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
    wr.writeBytes(body);
    wr.flush();
    wr.close();
  }

  private boolean isResponseCodeEqualNoContent(int responseCode) {
    return responseCode == HTTP_NO_CONTENT;
  }
}
