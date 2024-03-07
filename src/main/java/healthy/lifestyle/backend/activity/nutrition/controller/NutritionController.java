package healthy.lifestyle.backend.activity.nutrition.controller;

import healthy.lifestyle.backend.activity.nutrition.dto.NutritionResponseDto;
import healthy.lifestyle.backend.activity.nutrition.service.NutritionService;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/nutritions")
public class NutritionController {
    @Autowired
    NutritionService nutritionService;

    @GetMapping("/default/{nutrition_id}")
    public ResponseEntity<NutritionResponseDto> getDefaultNutritionById(
            @PathVariable("nutrition_id") @IdValidation long nutrition_id) {
        NutritionResponseDto responseDto = nutritionService.getNutritionById(nutrition_id, true, null);
        return ResponseEntity.ok(responseDto);
    }
}
