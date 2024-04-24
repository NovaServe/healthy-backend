package healthy.lifestyle.backend.activity.nutrition.service;

import healthy.lifestyle.backend.activity.nutrition.dto.NutritionResponseDto;

public interface NutritionService {
    NutritionResponseDto getNutritionById(long nutritionId, boolean requiredDefault, Long userId);
}
