package healthy.lifestyle.backend.admin.workout.controller;

import healthy.lifestyle.backend.admin.workout.service.ExerciseAdminService;
import healthy.lifestyle.backend.validation.*;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import java.util.List;
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
    private final ExerciseAdminService exerciseAdminService;

    public ExerciseAdminController(ExerciseAdminService exerciseAdminService) {
        this.exerciseAdminService = exerciseAdminService;
    }

    @GetMapping("/exercises")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ExerciseResponseDto>> getExercisesWithFilter(
            @RequestParam(name = "title", required = false) @TitleValidation String title,
            @RequestParam(name = "description", required = false) @DescriptionValidation String description,
            @RequestParam(name = "isCustom", required = false) Boolean isCustom,
            @RequestParam(name = "needsEquipment", required = false) Boolean needsEquipment) {
        return ResponseEntity.ok(
                exerciseAdminService.getExercisesWithFilter(title, description, isCustom, needsEquipment));
    }
}
