package healthy.lifestyle.backend.workout.controller;

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

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> createCustomWorkout(
            @Valid @RequestBody WorkoutCreateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutResponseDto responseDto = workoutService.createCustomWorkout(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/default/{workout_id}")
    public ResponseEntity<WorkoutResponseDto> getDefaultWorkoutById(@PathVariable("workout_id") long workoutId) {
        WorkoutResponseDto responseDto = workoutService.getWorkoutById(workoutId, false);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{workout_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> getCustomWorkoutById(@PathVariable("workout_id") long workoutId) {
        WorkoutResponseDto responseDto = workoutService.getWorkoutById(workoutId, true);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/default")
    public ResponseEntity<List<WorkoutResponseDto>> getDefaultWorkouts(
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        if (sortBy == null) sortBy = "id";
        boolean isDefault = true;
        Long userId = null;
        List<WorkoutResponseDto> responseDtoList = workoutService.getWorkouts(sortBy, isDefault, userId);
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<WorkoutResponseDto>> getCustomWorkouts(
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        if (sortBy == null) sortBy = "id";
        boolean isDefault = false;
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        List<WorkoutResponseDto> responseDtoList = workoutService.getWorkouts(sortBy, isDefault, userId);
        return ResponseEntity.ok(responseDtoList);
    }

    @PatchMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> updateCustomWorkout(
            @PathVariable long workoutId, @Valid @RequestBody WorkoutUpdateRequestDto requestDto) {
        Long userId = authService.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutResponseDto responseDto = workoutService.updateCustomWorkout(userId, workoutId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
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
