package pl.hellopolandticket.model.util;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Setter
@Getter
public class Discount {

  @Column(name = "discount_is_hpl_owner")
  boolean isHplOwner;

  @Column(name = "discount_value")
  int value;

  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type")
  Type type;

  @Column(name = "discount_hpl_part")
  int hplPart;

  @Column(name = "discount_partner_part")
  int partnerPart;

  public static enum Type {
    PERCENT, FLAT;
  }

}
