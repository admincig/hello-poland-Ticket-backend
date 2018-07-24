package pl.hellopolandticket.security;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import lombok.extern.slf4j.Slf4j;
import pl.hellopolandticket.dao.UserDao;
import pl.hellopolandticket.security.password.PasswordEncoder;

@Slf4j
@RequestScoped
public class JpaIdentityStore implements IdentityStore {

  @Inject
  private UserDao userDao;

  @Inject
  private PasswordEncoder passwordEncoder;

  @Override
  public CredentialValidationResult validate(Credential credential) {
    CredentialValidationResult credentialValidationResult;

    if (credential instanceof UsernamePasswordCredential) {
      UsernamePasswordCredential usernamePassword = (UsernamePasswordCredential) credential;

      credentialValidationResult = userDao.findByEmailAndNotHidden(usernamePassword.getCaller())
          .filter(u -> passwordEncoder
              .matches(new String(usernamePassword.getPassword().getValue()), u.getPassword()))
          .map(
              u -> new CredentialValidationResult(usernamePassword.getCaller(), u.getAuthorities()))
          .orElse(INVALID_RESULT);

    } else {
      credentialValidationResult = NOT_VALIDATED_RESULT;
    }
    return credentialValidationResult;
  }

  @Override
  public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
    return validationResult.getCallerGroups();
  }

}
