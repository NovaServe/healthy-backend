package healthy.lifestyle.backend.nutrition.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NutritionResponseDto {
    private long id;
    private String title;
    private String description;
    private boolean isCustom;
    private NutritionTypeResponseDto type;
}
