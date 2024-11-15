package pl.hellopolandticket.model.config;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Entity
@Table(name = "EMAIL_TEMPLATES")
@EqualsAndHashCode
@NoArgsConstructor
@ToString(exclude = "template")
public class EmailTemplate implements Serializable {

  private static final long serialVersionUID = 7935246378402298821L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "EMAIL_TEMPLATE_ID")
  private Long id;

  @Setter
  @NotNull
  @Column(name = "NAME", nullable = false, unique = true)
  private String name;

  @Setter
  @NotNull
  @Column(name = "SUBJECT", nullable = false)
  private String subject;

  @Setter
  @NotNull
  @Column(name = "TEMPLATE", nullable = false, length = 10000)
  private String template;

  @Builder
  public EmailTemplate(String name, String subject, String template) {
    this.name = name;
    this.subject = subject;
    this.template = template;
  }
}
