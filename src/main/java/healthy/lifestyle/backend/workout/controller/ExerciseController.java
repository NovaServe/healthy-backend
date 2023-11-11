package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.common.AuthUtil;
import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts/exercises")
public class ExerciseController {
    private final ExerciseService exerciseService;
    private final AuthService authService;
    private final AuthUtil authUtil;

    public ExerciseController(ExerciseService exerciseService, AuthService authService, AuthUtil authUtil) {
        this.exerciseService = exerciseService;
        this.authService = authService;
        this.authUtil = authUtil;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> createExercise(@RequestBody CreateExerciseRequestDto requestDto) {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(exerciseService.createExercise(requestDto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/default")
    public ResponseEntity<List<ExerciseResponseDto>> getDefaultExercises() {
        return ResponseEntity.ok(exerciseService.getDefaultExercises());
    }

    @GetMapping("/default/{exercise_id}")
    public ResponseEntity<ExerciseResponseDto> getExerciseById(@PathVariable("exercise_id") long exercise_id) {
        return ResponseEntity.ok(exerciseService.getDefaultExerciseById(exercise_id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<ExerciseResponseDto>> getCustomExercises() {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(exerciseService.getCustomExercises(userId));
    }
}
