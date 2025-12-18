package pl.hellopolandticket.model.sightevent;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class PdfAttachment {

    @Column(name = "pdfattachmentspaths", nullable = false)
    private String path;

    @Column(name = "original_name")
    private String originalName;

}
