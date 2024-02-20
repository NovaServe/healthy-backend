package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleValidation;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseCreateRequestDto {
    @TitleValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    @NotNull private boolean needsEquipment;

    @NotEmpty
    private List<Long> bodyParts;

    private List<Long> httpRefs;
}
