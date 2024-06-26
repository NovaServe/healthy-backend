package healthy.lifestyle.backend.activity.nutrition.dto;

import healthy.lifestyle.backend.activity.workout.dto.HttpRefResponseDto;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionResponseDto {
    private long id;

    private String title;

    private String description;

    private boolean isCustom;

    private List<HttpRefResponseDto> httpRefs;

    private NutritionTypeResponseDto type;
}
