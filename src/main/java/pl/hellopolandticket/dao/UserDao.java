    package pl.hellopolandticket.dao;

    import java.util.List;
    import java.util.Optional;
    import jakarta.ejb.LocalBean;
    import jakarta.ejb.Stateless;
    import jakarta.inject.Inject;
    import jakarta.persistence.EntityManager;
    import jakarta.persistence.PersistenceContext;
    import jakarta.persistence.PersistenceException;
    import pl.hellopolandticket.model.auth.User;
    import pl.hellopolandticket.model.partner.Partner;
    import pl.hellopolandticket.service.exception.ExceptionFactory;
    import pl.hellopolandticket.service.exception.conflict.ConflictingException;

    @Stateless
    @LocalBean
    public class UserDao {

      @PersistenceContext
      private EntityManager entityManager;
      @Inject
      private ExceptionFactory exceptionFactory;

      public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
      }

      public Optional<User> findByEmail(String email) {
        return entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
            .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
      }

      public Optional<User> findExternalUserForPartner(Partner partner) {
        return entityManager.createQuery(
                "from User user where user.partner = :partner "
                    + "and :role in elements(user.authorities)", User.class)
            .setParameter("partner", partner)
            .setParameter("role", pl.hellopolandticket.model.auth.Role.ROLE_EXTERNAL_USER)
            .getResultStream()
            .findFirst();
      }

      public User findByEmailOrThrowException(String email) {
        return entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
            .setParameter("email", email.toLowerCase()).getResultStream().findFirst()
            .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
      }

      public Optional<User> findByEmailWithAuthorities(String email) {
        Optional<User> optional =
            entityManager.createQuery("from User user where lower(user.email) = :email", User.class)
                .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
        if (optional.isPresent()) {
          optional.get().getAuthorities().size();
        }
        return optional;
      }

      public Optional<User> findByEmailAndNotHidden(String email) {
        Optional<User> optional =
            entityManager
                .createQuery("from User user where lower(user.email) = :email and user.hidden=false",
                    User.class)
                .setParameter("email", email.toLowerCase()).getResultStream().findFirst();
        if (optional.isPresent()) {
          optional.get().getAuthorities().size();
        }
        return optional;
      }

      public Optional<User> findByToken(String token) {
        return entityManager.createQuery("from User user where user.token=:token", User.class)
            .setParameter("token", token).getResultStream().findFirst();
      }

      public Optional<User> findByTokenWithAuthorities(String token) {
        Optional<User> optional =
            entityManager.createQuery("from User user where user.token=:token", User.class)
                .setParameter("token", token).getResultStream().findFirst();
        if (optional.isPresent()) {
          optional.get().getAuthorities().size();
        }
        return optional;
      }

      public User persist(User user) {
        try {
          entityManager.persist(user);
        } catch (PersistenceException e) {
          if (e.getCause().getClass().getName()
              .equals("org.hibernate.exception.ConstraintViolationException")) {
            throw new ConflictingException("Użytkownik już istnieje w systemie");
          }
        }
        return user;
      }

        public List<User> getUsersByCurrentUserAndRole(User loggedUser, String role) {
            return entityManager.createQuery(
                            "from User u " +
                                    "where u.partner = :partner " +
                                    "and u != :loggedUser " +
                                    "and u.hidden = false " +
                                    "and :role in elements(u.authorities)",
                            User.class)
                    .setParameter("partner", loggedUser.getPartner())
                    .setParameter("loggedUser", loggedUser)
                    .setParameter("role", role)
                    .getResultList();
        }

        public User getUserByRoleForCurrentPartner(long userId, Partner partner, String role) {
            return entityManager.createQuery(
                            "from User u " +
                                    "where u.id = :id " +
                                    "and u.partner = :partner " +
                                    "and u.hidden = false " +
                                    "and :role in elements(u.authorities)",
                            User.class)
                    .setParameter("id", userId)
                    .setParameter("partner", partner)
                    .setParameter("role", role)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
        }

        public User getUserForCurrnetPartner(long userId, Partner partner) {
            return entityManager.createQuery(
                            "from User u " +
                                    "where u.id = :id " +
                                    "and u.partner = :partner " +
                                    "and u.hidden = false",
                            User.class)
                    .setParameter("id", userId)
                    .setParameter("partner", partner)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> exceptionFactory.resourceNotFoundException());
        }

      public List<User> getUsersForPartner(Partner partner) {
        return entityManager.createQuery("from User u where u.partner = :partner", User.class)
            .setParameter("partner", partner).getResultList();
      }

      public User updateUser(User user) {
        return entityManager.merge(user);
      }

      public void removeAllUsersForPartner(Partner partner) {
        var users = getUsersForPartner(partner);
        users.forEach(u -> entityManager.remove(u));
      }

    }
