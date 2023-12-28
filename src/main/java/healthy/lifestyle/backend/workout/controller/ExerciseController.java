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
    public ResponseEntity<ExerciseResponseDto> createCustomExercise(
            @Valid @RequestBody ExerciseCreateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        ExerciseResponseDto responseDto = exerciseService.createCustomExercise(requestDto, userId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/default/{exercise_id}")
    public ResponseEntity<ExerciseResponseDto> getDefaultExerciseById(@PathVariable("exercise_id") long exercise_id) {
        ExerciseResponseDto responseDto = exerciseService.getExerciseById(exercise_id, true, null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{exercise_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> getCustomExerciseById(@PathVariable("exercise_id") long exercise_id) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        ExerciseResponseDto responseDto = exerciseService.getExerciseById(exercise_id, false, userId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/default")
    public ResponseEntity<List<ExerciseResponseDto>> getDefaultExercises() {
        List<ExerciseResponseDto> responseDtoList = exerciseService.getDefaultExercises();
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<ExerciseResponseDto>> getCustomExercises() {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        List<ExerciseResponseDto> responseDtoList = exerciseService.getCustomExercises(userId);
        return ResponseEntity.ok(responseDtoList);
    }

    @PatchMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ExerciseResponseDto> updateCustomExercise(
            @PathVariable("exerciseId") long exerciseId, @Valid @RequestBody ExerciseUpdateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        ExerciseResponseDto responseDto = exerciseService.updateCustomExercise(exerciseId, userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{exerciseId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomExercise(@PathVariable("exerciseId") long exerciseId) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        exerciseService.deleteCustomExercise(exerciseId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
