package healthy.lifestyle.backend.admin.controller;

import healthy.lifestyle.backend.admin.service.AdminService;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import org.springframework.web.bind.annotation.*;
import static java.util.Objects.isNull;
import java.util.List;

@RestController
@RequestMapping("${api.basePath}/${api.version}/admin")
public class AdminController {
    private final ExerciseService exerciseService;

    private final AdminService adminService;

    public AdminController(
            ExerciseService exerciseService,
            AdminService adminService) {
        this.exerciseService = exerciseService;
        this.adminService = adminService;
    }

    @GetMapping("/exercises")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ExerciseResponseDto>> getCustomExercises(){
        return ResponseEntity.ok(adminService.getCustomExercises());
    }

    @GetMapping("/exercises/default")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ExerciseResponseDto>> getDefaultExercises(){
        return ResponseEntity.ok(exerciseService.getDefaultExercises());
    }
}