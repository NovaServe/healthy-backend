package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.ExerciseCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import healthy.lifestyle.backend.workout.service.ExerciseService;
import jakarta.validation.Valid;
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

    public ExerciseController(ExerciseService exerciseService, AuthService authService) {
        this.exerciseService = exerciseService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> createCustomExercise(@RequestBody ExerciseCreateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(exerciseService.createExercise(requestDto, userId), HttpStatus.CREATED);
    }

    @GetMapping("/default/{exercise_id}")
    public ResponseEntity<ExerciseResponseDto> getDefaultExerciseById(@PathVariable("exercise_id") long exercise_id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(exercise_id, true, null));
    }

    @GetMapping("/{exercise_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> getCustomExerciseById(@PathVariable("exercise_id") long exercise_id) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(exerciseService.getExerciseById(exercise_id, false, userId));
    }

    @GetMapping("/default")
    public ResponseEntity<List<ExerciseResponseDto>> getDefaultExercises() {
        return ResponseEntity.ok(exerciseService.getDefaultExercises());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<ExerciseResponseDto>> getCustomExercises() {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(exerciseService.getCustomExercises(userId));
    }

    @PatchMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> updateCustomExercise(
            @PathVariable("exerciseId") long exerciseId, @Valid @RequestBody ExerciseUpdateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(exerciseService.updateCustomExercise(exerciseId, userId, requestDto));
    }

    @DeleteMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Long> deleteCustomExercise(@PathVariable("exerciseId") long exerciseId) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(exerciseService.deleteCustomExercise(exerciseId, userId), HttpStatus.NO_CONTENT);
    }
}
