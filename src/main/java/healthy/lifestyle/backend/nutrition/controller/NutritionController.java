package healthy.lifestyle.backend.nutrition.controller;

import healthy.lifestyle.backend.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.nutrition.service.NutritionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/nutritions")
public class NutritionController {

    private final NutritionService nutritionService;

    public NutritionController(NutritionService nutritionService) {
        this.nutritionService = nutritionService;
    }

    @GetMapping("/default/{nutrition_id}")
    public ResponseEntity<NutritionResponseDto> getDefaultNutritionById(
            @PathVariable("nutrition_id") long nutrition_id) {
        NutritionResponseDto responseDto = nutritionService.getNutritionById(nutrition_id, true, null);
        return ResponseEntity.ok(responseDto);
    }
}
