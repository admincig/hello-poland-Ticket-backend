package pl.hellopolandticket.service.csv.pojo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;
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
@JsonPropertyOrder(value = {"name", "price", "predefinedDate", "date", "sightName"})
public class TicketCSV implements Serializable {

  private static final long serialVersionUID = 4461566918374139044L;

  @NotNull
  private String name;

  @NotNull
  private Integer price;

  @NotNull
  private Boolean predefinedDate;

  private Date date;

  @NotNull
  private String sightName;

}