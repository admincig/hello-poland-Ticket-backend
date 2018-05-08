package pl.hellopolandticket.security.jwt;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static pl.hellopolandticket.security.jwt.TokenType.ACCESS_TOKEN;
import static pl.hellopolandticket.security.jwt.TokenType.REFRESH_TOKEN;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;
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
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.ExpiredTokenService;
import pl.hellopolandticket.service.dto.UserAuthDTO;
import pl.hellopolandticket.service.exception.preconditionfailed.TokenInExpiredTokensListException;

@Slf4j
@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {

  private static final String AUTHORIZATION_PREFIX = "Bearer ";
  private static final String AUTHENTICATION_METHOD = "POST";

  private static final String LOGIN_REQUEST_PATH = "/auth/login";
  private static final String REFRESH_TOKEN_REQUEST_PATH = "/auth/refresh";
  private static final String LOGOUT_REQUEST_PATH = "/auth/logout";

  @Inject
  private IdentityStoreHandler identityStoreHandler;

  @Inject
  private TokenProvider tokenProvider;

  @Inject
  @Authenticated
  private Event<CurrentUser> authenticatedEvent;

  @Inject
  private ExpiredTokenService expiredTokenService;

  @Override
  public AuthenticationStatus validateRequest(HttpServletRequest request,
      HttpServletResponse response, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    Optional<UserAuthDTO> userAuthDTO = extractUserAuthDTO(request);

    String login = userAuthDTO.map(UserAuthDTO::getLogin).orElse(null);
    String password = userAuthDTO.map(UserAuthDTO::getPassword).orElse(null);

    String accessToken = userAuthDTO.map(UserAuthDTO::getAccessToken).orElse(null);
    String refreshToken = userAuthDTO.map(UserAuthDTO::getRefreshToken).orElse(null);

    String authorizationToken = extractToken(context);

    if (isLoginRequest(request)) {
      if (hasProperDataToLogin(login, password)) {
        authenticationStatus = login(login, password, context);
      } else {
        authenticationStatus = context.responseUnauthorized();
      }
    } else if (isRefreshingRequest(authorizationToken, request)) {
      authenticationStatus = validateRefreshToken(authorizationToken, context);
    } else if (isLogoutRequest(accessToken, refreshToken, request)) {
      authenticationStatus = logout(accessToken, refreshToken, context);
    } else if (authorizationToken != null) {
      authenticationStatus = validateAccessToken(authorizationToken, context);
    } else if (context.isProtected()) {
      authenticationStatus = context.responseUnauthorized();
    } else {
      authenticationStatus = context.doNothing();
    }

    return authenticationStatus;
  }

  private Optional<UserAuthDTO> extractUserAuthDTO(HttpServletRequest request) {
    Optional<UserAuthDTO> userAuthDTO = empty();
    String userAuthJson = "";

    try {
      userAuthJson = new BufferedReader(
          new InputStreamReader(request.getInputStream())).lines()
          .collect(joining("\n"));
    } catch (Exception ignored) {
    }

    if (!userAuthJson.isEmpty()) {
      userAuthDTO = ofNullable(JsonbBuilder.create()
          .fromJson(userAuthJson, UserAuthDTO.class));
    }

    return userAuthDTO;
  }

  private AuthenticationStatus validateAccessToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      validateTokenNotInExpiredTokensList(token);
      tokenProvider.validateToken(token, ACCESS_TOKEN);
      JwtCredential credential = tokenProvider.getCredential(token, ACCESS_TOKEN);

      authenticatedEvent.fire(
          CurrentUser.builder()
              .email(credential.getPrincipal())
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

  private boolean isLoginRequest(HttpServletRequest request) {
    return AUTHENTICATION_METHOD.equals(request.getMethod())
        && request.getRequestURI().endsWith(LOGIN_REQUEST_PATH);
  }

  private boolean hasProperDataToLogin(String email, String password) {
    return email != null && password != null;
  }

  private boolean isRefreshingRequest(String token, HttpServletRequest request) {
    return token != null
        && AUTHENTICATION_METHOD.equals(request.getMethod())
        && request.getRequestURI().endsWith(REFRESH_TOKEN_REQUEST_PATH);
  }

  private boolean isLogoutRequest(String accessToken, String refreshToken,
      HttpServletRequest request) {
    return accessToken != null && refreshToken != null
        && AUTHENTICATION_METHOD.equals(request.getMethod())
        && request.getRequestURI().endsWith(LOGOUT_REQUEST_PATH);
  }

  private AuthenticationStatus login(String login, String password, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    CredentialValidationResult credentialValidationResult = identityStoreHandler
        .validate(new UsernamePasswordCredential(login, password));

    if (loggedCorrectly(credentialValidationResult.getStatus())) {
      authenticationStatus = createToken(credentialValidationResult, context);
    } else {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private AuthenticationStatus logout(String accessToken, String refreshToken,
      HttpMessageContext context) {
    addOldTokenToExpiredTokensList(accessToken);
    addOldTokenToExpiredTokensList(refreshToken);

    return context.doNothing();
  }

  private AuthenticationStatus validateRefreshToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      validateTokenNotInExpiredTokensList(token);
      tokenProvider.validateToken(token, REFRESH_TOKEN);

      JwtCredential jwtCredential = tokenProvider.getCredential(token, REFRESH_TOKEN);

      addOldTokenToExpiredTokensList(token);

      authenticationStatus = createToken(jwtCredential, context);
    } catch (Exception e) {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private void validateTokenNotInExpiredTokensList(String token) {
    if (expiredTokenService.isTokenInExpiredTokensList(token)) {
      throw new TokenInExpiredTokensListException();
    }
  }

  private void addOldTokenToExpiredTokensList(String token) {
    expiredTokenService.addTokenToExpiredTokensList(token);
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
        CurrentUser.builder()
            .email(result.getCallerPrincipal().getName())
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
        CurrentUser.builder()
            .email(jwtCredential.getPrincipal())
            .roles(jwtCredential.getAuthorities())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build());

    return context
        .notifyContainerAboutLogin(jwtCredential.getPrincipal(), jwtCredential.getAuthorities());
  }

}
