package pl.hellopolandticket.security.jwt;

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.CredentialValidationResult.Status;
import javax.security.enterprise.identitystore.IdentityStoreHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.UserInfo;

@Slf4j
@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {

  private static final String AUTHORIZATION_PREFIX = "Bearer ";

  @Inject
  private IdentityStoreHandler identityStoreHandler;

  @Inject
  private TokenProvider tokenProvider;

  @Inject
  @Authenticated
  private Event<UserInfo> authenticatedEvent;

  @Override
  public AuthenticationStatus validateRequest(HttpServletRequest request,
      HttpServletResponse response, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    String name = request.getParameter("username");
    String password = request.getParameter("password");

    String token = extractToken(context);

    if (isLoginPost(name, password, request)) {
      authenticationStatus = login(name, password, context);
    } else if (token != null) {
      authenticationStatus = validateToken(token, context);
    } else if (context.isProtected()) {
      authenticationStatus = context.responseUnauthorized();
    } else {
      authenticationStatus = context.doNothing();
    }

    return authenticationStatus;
  }

  private AuthenticationStatus validateToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      tokenProvider.validateToken(token);
      JwtCredential credential = tokenProvider.getCredential(token);

      authenticatedEvent
          .fire(new UserInfo(credential.getPrincipal(), credential.getAuthorities()));

      authenticationStatus = context
          .notifyContainerAboutLogin(credential.getPrincipal(), credential.getAuthorities());

    } catch (Exception eje) {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private String extractToken(HttpMessageContext context) {
    String token = null;
    String authorizationHeader = context.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

    if (hasAuthorizationHeader(authorizationHeader)) {
      token = authorizationHeader
          .substring(AUTHORIZATION_PREFIX.length(), authorizationHeader.length());
    }

    return token;
  }

  private boolean hasAuthorizationHeader(String authorizationHeader) {
    return authorizationHeader != null && authorizationHeader.startsWith(AUTHORIZATION_PREFIX);
  }

  private boolean isLoginPost(String name, String password, HttpServletRequest request) {
    return name != null && password != null
        && "POST".equals(request.getMethod())
        && request.getRequestURI().endsWith("/auth/login");
  }

  private AuthenticationStatus login(String name, String password, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    CredentialValidationResult credentialValidationResult = identityStoreHandler
        .validate(new UsernamePasswordCredential(name, password));

    if (loggedCorrectly(credentialValidationResult.getStatus())) {
      authenticationStatus = createToken(credentialValidationResult, context);
    } else {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private boolean loggedCorrectly(Status status) {
    return status == VALID;
  }

  private AuthenticationStatus createToken(CredentialValidationResult result,
      HttpMessageContext context) {

    String jwt = tokenProvider
        .createToken(result.getCallerPrincipal().getName(), result.getCallerGroups());
    context.getResponse().setHeader(HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + jwt);

    authenticatedEvent
        .fire(new UserInfo(result.getCallerPrincipal().getName(), result.getCallerGroups()));

    return context.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
  }
}
