package healthy.lifestyle.backend.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpRefRequestDto {
    @IdValidation
    private long id;
}
