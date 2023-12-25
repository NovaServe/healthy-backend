package healthy.lifestyle.backend.admin.exercises.controller;

import healthy.lifestyle.backend.admin.exercises.service.ExerciseAdminService;
import healthy.lifestyle.backend.validation.*;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("${api.basePath}/${api.version}/admin")
public class ExerciseAdminController {

    private final ExerciseAdminService exerciseAdminService;

    public ExerciseAdminController(ExerciseAdminService exerciseAdminService) {

        this.exerciseAdminService = exerciseAdminService;
    }

    @GetMapping("/exercises")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ExerciseResponseDto>> getAllExercises(
            @RequestParam(name = "title", required = false) @TitleValidation String title,
            @RequestParam(name = "description", required = false) @DescriptionValidation String description,
            @RequestParam(name = "isCustom", required = false) boolean isCustom,
            @RequestParam(name = "needsEquipment", required = false) boolean needsEquipment) {
        return ResponseEntity.ok(
                exerciseAdminService.getExercisesByFilters(title, description, isCustom, needsEquipment));
    }
}
