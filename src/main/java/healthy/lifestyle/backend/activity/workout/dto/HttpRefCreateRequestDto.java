package healthy.lifestyle.backend.activity.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleValidation;
import healthy.lifestyle.backend.shared.validation.annotation.WebLinkValidation;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRefCreateRequestDto {
    @TitleValidation
    private String name;

    @DescriptionOptionalValidation
    private String description;

    @WebLinkValidation
    private String ref;

    @NotBlank
    private String httpRefType;
}
