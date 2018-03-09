package pl.hellopolandticket.model;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@LocalBean
@Stateless
public class Sample {

  public Sample(String name) {
    this.name = name;
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
