package healthy.lifestyle.backend.admin.workout.controller;

import healthy.lifestyle.backend.activity.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.admin.workout.service.ExerciseAdminService;
import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/admin")
public class ExerciseAdminController {
    @Autowired
    ExerciseAdminService exerciseAdminService;

    @Operation(summary = "Get default and custom exercises (admin)")
    @GetMapping("/exercises")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ExerciseResponseDto>> getExercisesWithFilter(
            @RequestParam(name = "title", required = false) @TitleOptionalValidation String title,
            @RequestParam(name = "description", required = false) @DescriptionOptionalValidation String description,
            @RequestParam(name = "isCustom", required = false) Boolean isCustom,
            @RequestParam(name = "needsEquipment", required = false) Boolean needsEquipment) {
        return ResponseEntity.ok(
                exerciseAdminService.getExercisesWithFilter(title, description, isCustom, needsEquipment));
    }
}
