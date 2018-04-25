package pl.hellopolandticket.security.jwt;

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static pl.hellopolandticket.security.jwt.TokenType.ACCESS_TOKEN;
import static pl.hellopolandticket.security.jwt.TokenType.REFRESH_TOKEN;

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
import pl.hellopolandticket.service.BlackTokenService;
import pl.hellopolandticket.service.exception.preconditionfailed.TokenInBlackListException;

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

  @Inject
  private BlackTokenService blackTokenService;

  @Override
  public AuthenticationStatus validateRequest(HttpServletRequest request,
      HttpServletResponse response, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    String name = request.getParameter("username");
    String password = request.getParameter("password");

    String token = extractToken(context);

    if (isLoginRequest(name, password, request)) {
      authenticationStatus = login(name, password, context);
    } else if (isRefreshingRequest(token, request)) {
      authenticationStatus = validateRefreshToken(token, context);
    } else if (token != null) {
      authenticationStatus = validateAccessToken(token, context);
    } else if (context.isProtected()) {
      authenticationStatus = context.responseUnauthorized();
    } else {
      authenticationStatus = context.doNothing();
    }

    return authenticationStatus;
  }

  private AuthenticationStatus validateAccessToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      validateTokenNotInTokensBlackList(token);
      tokenProvider.validateToken(token, ACCESS_TOKEN);
      JwtCredential credential = tokenProvider.getCredential(token, ACCESS_TOKEN);

      authenticatedEvent.fire(
          UserInfo.builder()
              .name(credential.getPrincipal())
              .roles(credential.getAuthorities())
              .build()
      );

      authenticationStatus = context
          .notifyContainerAboutLogin(credential.getPrincipal(), credential.getAuthorities());

    } catch (Exception e) {
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

  private boolean isLoginRequest(String name, String password, HttpServletRequest request) {
    return name != null && password != null
        && "POST".equals(request.getMethod())
        && request.getRequestURI().endsWith("/auth/login");
  }

  private boolean isRefreshingRequest(String token, HttpServletRequest request) {
    return token != null
        && "POST".equals(request.getMethod())
        && request.getRequestURI().endsWith("/auth/refresh");
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

  private AuthenticationStatus validateRefreshToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      validateTokenNotInTokensBlackList(token);
      tokenProvider.validateToken(token, REFRESH_TOKEN);

      JwtCredential jwtCredential = tokenProvider.getCredential(token, REFRESH_TOKEN);

      addOldTokenToBlackList(token);

      authenticationStatus = createToken(jwtCredential, context);
    } catch (Exception e) {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private void validateTokenNotInTokensBlackList(String token) {
    if (blackTokenService.isTokenInBlackList(token)) {
      throw new TokenInBlackListException();
    }
  }

  private void addOldTokenToBlackList(String token) {
    blackTokenService.addTokenToBlackList(token);
  }

  private boolean loggedCorrectly(Status status) {
    return status == VALID;
  }

  private AuthenticationStatus createToken(CredentialValidationResult result,
      HttpMessageContext context) {

    String accessToken = tokenProvider
        .createToken(result.getCallerPrincipal().getName(), result.getCallerGroups(), ACCESS_TOKEN);

    String refreshToken = tokenProvider
        .createToken(result.getCallerPrincipal().getName(), result.getCallerGroups(),
            REFRESH_TOKEN);

    authenticatedEvent.fire(
        UserInfo.builder()
            .name(result.getCallerPrincipal().getName())
            .roles(result.getCallerGroups())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build());

    return context.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
  }

  private AuthenticationStatus createToken(JwtCredential jwtCredential,
      HttpMessageContext context) {

    String accessToken = tokenProvider
        .createToken(jwtCredential.getPrincipal(), jwtCredential.getAuthorities(), ACCESS_TOKEN);

    String refreshToken = tokenProvider
        .createToken(jwtCredential.getPrincipal(), jwtCredential.getAuthorities(), REFRESH_TOKEN);

    authenticatedEvent.fire(
        UserInfo.builder()
            .name(jwtCredential.getPrincipal())
            .roles(jwtCredential.getAuthorities())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build());

    return context
        .notifyContainerAboutLogin(jwtCredential.getPrincipal(), jwtCredential.getAuthorities());
  }

}
