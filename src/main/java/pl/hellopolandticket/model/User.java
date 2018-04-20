package pl.hellopolandticket.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "USERS")
@EqualsAndHashCode
@NoArgsConstructor
public class User implements Serializable {

  private static final long serialVersionUID = 7266276164148705023L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "USER_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "USERNAME", nullable = false, unique = true)
  private String username;


  @Setter
  @NotNull
  @Column(name = "PASSWORD", nullable = false)
  private String password;

  @Setter
  @NotNull
  @Column(name = "EMAIL", nullable = false, unique = true)
  @Email
  private String email;

  @ElementCollection
  private Set<String> authorities = new HashSet<>();

  @Builder
  public User(String username, String password, String email, Set<String> authorities) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.authorities = authorities;
  }
}
