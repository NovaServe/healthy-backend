package healthy.lifestyle.backend.nutrition.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NutritionTypeResponseDto {
    private long id;
    private String name;
}
