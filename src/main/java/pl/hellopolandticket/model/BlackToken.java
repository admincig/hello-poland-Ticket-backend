package pl.hellopolandticket.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@Table(name = "TOKENS_BLACK_LIST")
@EqualsAndHashCode
@NoArgsConstructor
public class BlackToken implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "TOKEN_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "TOKEN", nullable = false, unique = true)
  private String token;

  @Builder
  public BlackToken(String token) {
    this.token = token;
  }
}
