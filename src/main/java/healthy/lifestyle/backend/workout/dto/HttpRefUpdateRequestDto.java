package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.WebLinkOptionalValidation;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRefUpdateRequestDto {
    @TitleOptionalValidation
    private String name;

    @DescriptionOptionalValidation
    private String description;

    @WebLinkOptionalValidation
    private String ref;
}
