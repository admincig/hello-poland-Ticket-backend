package pl.hellopolandticket.security.jwt;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;
import static pl.hellopolandticket.security.jwt.TokenType.ACCESS_TOKEN;
import static pl.hellopolandticket.security.jwt.TokenType.REFRESH_TOKEN;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.stream.JsonParsingException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.CredentialValidationResult.Status;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import pl.hellopoland.dto.UserAuthDTO;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.model.auth.User;
import pl.hellopolandticket.security.Authenticated;
import pl.hellopolandticket.security.CurrentUser;
import pl.hellopolandticket.service.ExpiredTokenService;
import pl.hellopolandticket.service.exception.preconditionfailed.TokenInExpiredTokensListException;

@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {

  private static final String AUTHORIZATION_PREFIX = "Bearer ";
  private static final String AUTHENTICATION_METHOD = "POST";

  private static final String LOGIN_REQUEST_PATH = "/login";
  private static final String REFRESH_TOKEN_REQUEST_PATH = "/refresh";
  private static final String LOGOUT_REQUEST_PATH = "/logout";

  @Inject
  private IdentityStoreHandler identityStoreHandler;

  @Inject
  private TokenProvider tokenProvider;

  @Inject
  @Authenticated
  private Event<CurrentUser> authenticatedEvent;

  @Inject
  private ExpiredTokenService expiredTokenService;

  @Inject
  private UserDao userDao;

  @Override
  public AuthenticationStatus validateRequest(HttpServletRequest request,
      HttpServletResponse response, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus = null;

    String authorizationToken = extractToken(context);

    if (isAuthRequest(request)) {
      Optional<UserAuthDTO> userAuthDTO = extractUserAuthDTO(request);
      if (isLoginRequest(request)) {
        String login = userAuthDTO.map(u -> u.login).orElse(null);
        String password = userAuthDTO.map(u -> u.password).orElse(null);
        if (hasProperDataToLogin(login, password)) {
          authenticationStatus = login(login, password, context);
        } else {
          authenticationStatus = context.responseUnauthorized();
        }
      } else if (isRefreshingRequest(authorizationToken, request)) {
        authenticationStatus = validateRefreshToken(authorizationToken, context);
      } else if (isLogoutRequest(request)) {
        String accessToken = userAuthDTO.map(u -> u.accessToken).or(() -> Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).map(h -> h.replace(AUTHORIZATION_PREFIX, ""))).orElse(null);
        String refreshToken = userAuthDTO.map(u -> u.refreshToken).orElse(null);
        authenticationStatus = logout(accessToken, refreshToken, context);
      }
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
      userAuthJson = new BufferedReader(new InputStreamReader(request.getInputStream())).lines()
          .collect(joining("\n"));
    } catch (Exception ignored) {
    }

    if (!userAuthJson.isEmpty()) {
      try {
        userAuthDTO = ofNullable(JsonbBuilder.create().fromJson(userAuthJson, UserAuthDTO.class));
      } catch (JsonParsingException ignored) {
      }
    }

    return userAuthDTO;
  }

  private AuthenticationStatus validateAccessToken(String token, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    try {
      Optional<User> user = userDao.findByTokenWithAuthorities(token);

      if (user.isPresent()) {
        authenticationStatus = signInExistingUser(user.get(), context);
      } else {
        authenticationStatus = signInUser(token, context);
      }
    } catch (Exception e) {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private String extractToken(HttpMessageContext context) {
    String token = null;
    String authorizationHeader = context.getRequest().getHeader(HttpHeaders.AUTHORIZATION);

    if (hasAuthorizationHeader(authorizationHeader)) {
      token = authorizationHeader.substring(AUTHORIZATION_PREFIX.length(),
          authorizationHeader.length());
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


  private boolean isAuthRequest(HttpServletRequest request) {
    return request.getRequestURI().endsWith(LOGIN_REQUEST_PATH)
        || request.getRequestURI().endsWith(REFRESH_TOKEN_REQUEST_PATH)
        || request.getRequestURI().endsWith(LOGOUT_REQUEST_PATH);
  }

  private boolean isRefreshingRequest(String token, HttpServletRequest request) {
    return token != null && AUTHENTICATION_METHOD.equals(request.getMethod())
        && request.getRequestURI().endsWith(REFRESH_TOKEN_REQUEST_PATH);
  }

  private boolean isLogoutRequest(HttpServletRequest request) {
    return AUTHENTICATION_METHOD.equals(request.getMethod())
        && request.getRequestURI().endsWith(LOGOUT_REQUEST_PATH);
  }

  private AuthenticationStatus login(String login, String password, HttpMessageContext context) {
    AuthenticationStatus authenticationStatus;

    CredentialValidationResult credentialValidationResult =
        identityStoreHandler.validate(new UsernamePasswordCredential(login, password));

    if (loggedCorrectly(credentialValidationResult.getStatus())) {
      authenticationStatus = createToken(credentialValidationResult, context);
    } else {
      authenticationStatus = context.responseUnauthorized();
    }

    return authenticationStatus;
  }

  private AuthenticationStatus logout(String accessToken, String refreshToken,
      HttpMessageContext context) {
    if (StringUtils.isNotBlank(accessToken)) {
      addOldTokenToExpiredTokensList(accessToken);
    }
    if (StringUtils.isNotBlank(refreshToken)) {
      addOldTokenToExpiredTokensList(refreshToken);
    }

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

    String accessToken = tokenProvider.createToken(result.getCallerPrincipal().getName(),
        result.getCallerGroups(), ACCESS_TOKEN);

    String refreshToken = tokenProvider.createToken(result.getCallerPrincipal().getName(),
        result.getCallerGroups(), REFRESH_TOKEN);

    authenticatedEvent.fire(CurrentUser.builder().principal(result.getCallerPrincipal().getName())
        .roles(result.getCallerGroups()).accessToken(accessToken).refreshToken(refreshToken)
        .build());

    return context.notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
  }

  private AuthenticationStatus createToken(JwtCredential jwtCredential,
      HttpMessageContext context) {

    String accessToken = tokenProvider.createToken(jwtCredential.getPrincipal(),
        jwtCredential.getAuthorities(), ACCESS_TOKEN);

    String refreshToken = tokenProvider.createToken(jwtCredential.getPrincipal(),
        jwtCredential.getAuthorities(), REFRESH_TOKEN);

    authenticatedEvent.fire(CurrentUser.builder().principal(jwtCredential.getPrincipal())
        .roles(jwtCredential.getAuthorities()).accessToken(accessToken).refreshToken(refreshToken)
        .build());

    return context.notifyContainerAboutLogin(jwtCredential.getPrincipal(),
        jwtCredential.getAuthorities());
  }

  private AuthenticationStatus signInExistingUser(User partnerUser, HttpMessageContext context) {
    Set<String> roles = partnerUser.getAuthorities();

    authenticatedEvent
        .fire(CurrentUser.builder().principal(partnerUser.getEmail()).roles(roles).build());

    return context.notifyContainerAboutLogin(partnerUser.getEmail(), roles);
  }

  private AuthenticationStatus signInUser(String token, HttpMessageContext context) {
    validateTokenNotInExpiredTokensList(token);
    tokenProvider.validateToken(token, ACCESS_TOKEN);
    JwtCredential credential = tokenProvider.getCredential(token, ACCESS_TOKEN);

    authenticatedEvent.fire(CurrentUser.builder().principal(credential.getPrincipal())
        .roles(credential.getAuthorities()).build());

    return context.notifyContainerAboutLogin(credential.getPrincipal(),
        credential.getAuthorities());
  }
}
