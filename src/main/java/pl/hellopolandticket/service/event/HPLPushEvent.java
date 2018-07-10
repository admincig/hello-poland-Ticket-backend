package pl.hellopolandticket.service.event;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.hellopoland.dto.Push;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HPLPushEvent implements Serializable {

  private static final long serialVersionUID = 8907062191436005717L;

  private Push push;
  private String URLPath;
}
