package healthy.lifestyle.backend.nutrition.service;

import healthy.lifestyle.backend.nutrition.dto.NutritionResponseDto;

public interface NutritionService {
    NutritionResponseDto getNutritionById(long nutritionId, boolean requiredDefault, Long userId);
}
