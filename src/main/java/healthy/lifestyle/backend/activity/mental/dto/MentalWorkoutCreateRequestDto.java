package healthy.lifestyle.backend.activity.mental.dto;

import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleValidation;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentalWorkoutCreateRequestDto {

    @TitleValidation
    private String title;

    @DescriptionOptionalValidation
    private String description;

    @NotEmpty
    private List<Long> mentalActivityIds;
}
