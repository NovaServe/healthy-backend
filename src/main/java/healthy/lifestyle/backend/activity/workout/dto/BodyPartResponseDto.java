package healthy.lifestyle.backend.activity.workout.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyPartResponseDto {
    private long id;

    private String name;
}
