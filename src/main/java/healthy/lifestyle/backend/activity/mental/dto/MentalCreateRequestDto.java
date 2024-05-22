package healthy.lifestyle.backend.activity.mental.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleValidation;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentalCreateRequestDto {
    @TitleValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    private List<Long> httpRefs;

    @IdValidation
    private Long mentalTypeId;
}
