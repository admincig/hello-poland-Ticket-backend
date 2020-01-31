package pl.hellopolandticket.model.util;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import pl.hellopoland.dto.DiscountDTO;

@Embeddable
@Setter
@Getter
public class Discount {

  @Column(name = "discount_is_custom_comission")
  boolean isCustomComission;

  @Column(name = "discount_value")
  int value;

  @Column(name = "discount_amount")
  int amount;

  @Column(name = "discount_percent")
  Integer percent;

  @Column(name = "discount_price")
  int discountPrice;

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

  public boolean equals(DiscountDTO other) {
    return Objects.equals(hplPart, other.hplPart)
        && Objects.equals(isCustomComission, other.isCustomCommission)
        && Objects.equals(partnerPart, other.partnerPart)
        && Objects.equals(type.toString(), other.type.toString())
        && Objects.equals(value, other.value);
  }

}
