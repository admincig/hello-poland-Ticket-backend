package pl.hellopolandticket.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "PARTNERS")
@EqualsAndHashCode
@NoArgsConstructor
public class Partner implements Serializable {

  private static final long serialVersionUID = 6118414827783500940L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "PARTNER_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false, unique = true)
  private String name;

  @Setter
  @OneToMany(mappedBy = "partner")
  private List<User> users;

  @Setter
  @OneToMany(mappedBy = "partner")
  private List<Sight> sights;

  @Builder
  public Partner(String name) {
    this.name = name;
  }

}