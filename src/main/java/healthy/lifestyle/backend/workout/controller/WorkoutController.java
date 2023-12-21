package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.users.service.AuthService;
import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import healthy.lifestyle.backend.workout.service.WorkoutService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;

    private final AuthService authService;

    public WorkoutController(WorkoutService workoutService, AuthService authService) {
        this.workoutService = workoutService;
        this.authService = authService;
    }

    @GetMapping("/default")
    public ResponseEntity<List<WorkoutResponseDto>> getDefaultWorkouts(
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName) {
        if (isNull(sortFieldName)) sortFieldName = "id";
        return ResponseEntity.ok(workoutService.getDefaultWorkouts(sortFieldName));
    }

    @GetMapping("/default/{workout_id}")
    public ResponseEntity<WorkoutResponseDto> getDefaultWorkoutDetails(@PathVariable("workout_id") long workoutId) {
        return ResponseEntity.ok(workoutService.getWorkoutById(workoutId, false));
    }

    @GetMapping("/{workout_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> getCustomWorkoutDetails(@PathVariable("workout_id") long workoutId) {
        return ResponseEntity.ok(workoutService.getWorkoutById(workoutId, true));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> createCustomWorkout(
            @Valid @RequestBody WorkoutCreateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(workoutService.createCustomWorkout(userId, requestDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> updateCustomWorkout(
            @PathVariable long workoutId, @Valid @RequestBody WorkoutUpdateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        return new ResponseEntity<>(workoutService.updateCustomWorkout(userId, workoutId, requestDto), HttpStatus.OK);
    }

    @DeleteMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomWorkout(@PathVariable("workoutId") Long workoutId) {
        Long authenticatedUserId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        workoutService.deleteCustomWorkout(authenticatedUserId, workoutId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
