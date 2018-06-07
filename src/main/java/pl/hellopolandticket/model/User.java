package pl.hellopolandticket.model;

import static pl.hellopolandticket.model.UUIDGeneratorUtil.generateUUID;

import io.jsonwebtoken.Jwts;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "USERS")
@EqualsAndHashCode
@NoArgsConstructor
@ToString(exclude = {"password", "authorities", "token"})
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

  @Builder
  public static User createHiddenUser(String name, String email, Set<String> authorities,
      Partner partner) {
    User user = User.builder()
        .name(name)
        .email(email)
        .authorities(authorities)
        .partner(partner)
        .build();

    user.setHidden(true);

    user.setToken(Jwts.builder()
        .setSubject(generateUUID())
        .compact());

    return user;
  }
}