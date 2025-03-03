package pl.hellopolandticket.model.auth;

import io.jsonwebtoken.Jwts;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import pl.hellopolandticket.model.partner.Partner;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static pl.hellopolandticket.model.util.UUIDGeneratorUtil.generateUUID;

@Getter
@Entity
@Table(name = "USERS")
@EqualsAndHashCode(exclude = {"authorities"})
@NoArgsConstructor
@ToString(exclude = {"name", "password", "email", "authorities", "token"})
public class User implements Serializable {

  private static final long serialVersionUID = 7266276164148705023L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "USER_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false)
  private String name;

  @Setter
  @Column(name = "PASSWORD")
  private String password;

  @Setter
  @NotNull
  @Column(name = "EMAIL", nullable = false, unique = true)
  @Email
  private String email;

  @ElementCollection
  private Set<String> authorities = new HashSet<>();

  @Setter
  @ManyToOne
  @JoinColumn(name = "PARTNER_ID")
  private Partner partner;

  @Setter
  @Column(name = "TOKEN", unique = true)
  private String token;

  @Setter
  @NotNull
  @Column(name = "HIDDEN", nullable = false)
  private Boolean hidden;

  @Builder
  public User(String name, String password, String email, Set<String> authorities,
      Partner partner) {
    this.name = name;
    this.password = password;
    this.email = email;
    this.authorities = authorities;
    this.partner = partner;
    this.hidden = false;
  }

  public static User createHiddenUser(String name, String email, Set<String> authorities,
      Partner partner) {
    User user = User.builder().name(name).email(email.toLowerCase()).authorities(authorities)
        .partner(partner).build();
    user.setHidden(true);
    user.setToken(Jwts.builder().setSubject(generateUUID()).compact());
    return user;
  }

  public static User createUsher(String name, String email, String password, Partner partner) {
    User user = User.builder().name(name).email(email.toLowerCase())
        .authorities(Set.of(Role.ROLE_USHER)).partner(partner).build();
    user.setHidden(false);
    user.setPassword(password);
    return user;
  }

  public boolean hasRole(String role) {
    return this.authorities.contains(role);
  }

}
