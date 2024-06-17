package healthy.lifestyle.backend.plan.workout.dto;

import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanCreateRequestDto {
    @IdValidation
    private Long workoutId;

    @NotNull private LocalDate startDate; // in user's zone

    @NotNull private LocalDate endDate; // in user's zone

    @NotBlank
    private String jsonDescription; //  in user's zone: {"week_day": string enum, "hours": [0..23], "minutes": [0..59]}
}
