package healthy.lifestyle.backend.workout.dto;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExerciseResponseDto {
    private Long id;

    private String title;

    private String description;

    private boolean isCustom;

    private boolean needsEquipment;

    private List<BodyPartResponseDto> bodyParts;

    private List<HttpRefResponseDto> httpRefs;
}
