package pl.hellopolandticket.service.event;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopoland.dto.Sight;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SightsImportEvent implements Serializable {

  private static final long serialVersionUID = 5460790543014124128L;

  private List<Sight> sights;

}