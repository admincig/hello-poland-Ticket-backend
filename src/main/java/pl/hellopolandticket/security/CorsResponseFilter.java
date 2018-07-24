package pl.hellopolandticket.security;

import static java.lang.String.join;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class CorsResponseFilter implements ContainerResponseFilter {

  private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH";
  private final static int MAX_AGE = 24 * 60 * 60;
  private final static String DEFAULT_ALLOWED_HEADERS = "origin,accept,content-type,authorization";

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
    headers.add("Access-Control-Allow-Origin", "*");
    headers.add("Access-Control-Allow-Headers", getRequestedAllowedHeaders(requestContext));
    headers.add("Access-Control-Allow-Credentials", "true");
    headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS);
    headers.add("Access-Control-Max-Age", MAX_AGE);
    headers.add("x-responded-by", "cors-response-filter");
  }

  private String getRequestedAllowedHeaders(ContainerRequestContext responseContext) {
    List<String> headers = responseContext.getHeaders().get("Access-Control-Allow-Headers");
    return createHeaderList(headers, DEFAULT_ALLOWED_HEADERS);
  }

  private String createHeaderList(List<String> headers, String defaultHeaders) {
    if (headers == null || headers.isEmpty()) {
      return defaultHeaders;
    }
    String result = join(",", headers);
    result = result + "," + defaultHeaders;
    return result;
  }

}
