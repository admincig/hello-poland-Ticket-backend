package pl.hellopolandticket.service.dto;

import java.io.Serializable;
import java.util.Collection;
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
public class JsonCollectionWrapper implements Serializable {

  private static final long serialVersionUID = -4586883709119161119L;

  Collection<?> items;
}
