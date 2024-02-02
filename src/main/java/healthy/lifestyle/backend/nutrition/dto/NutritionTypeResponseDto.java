package healthy.lifestyle.backend.nutrition.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionTypeResponseDto {
    private long id;

    private String name;
}
