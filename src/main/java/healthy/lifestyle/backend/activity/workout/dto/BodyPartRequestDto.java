package healthy.lifestyle.backend.activity.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyPartRequestDto {
    @IdValidation
    private long id;
}
