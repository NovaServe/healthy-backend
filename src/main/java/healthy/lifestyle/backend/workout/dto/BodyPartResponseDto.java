package healthy.lifestyle.backend.workout.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BodyPartResponseDto {
    private long id;

    private String name;
}
