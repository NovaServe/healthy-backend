package healthy.lifestyle.backend.activity.mental.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentalUpdateRequestDto {
    @TitleOptionalValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    private List<Long> httpRefIds;

    @IdValidation
    private Long mentalTypeId;
}
