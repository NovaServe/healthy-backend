package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.validation.IdValidation;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyPartRequestDto {
    @NotNull @IdValidation
    private long id;
}
