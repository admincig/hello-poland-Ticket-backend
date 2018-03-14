package pl.hellopolandticket.service.csv.pojo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(value = {"name", "price", "predefinedDate", "date", "sight"})
public class TicketCSV implements Serializable {

  private static final long serialVersionUID = 4461566918374139044L;

  private String name;
  private Integer price;
  private Boolean predefinedDate;
  private Date date;
  private Long sight;
}