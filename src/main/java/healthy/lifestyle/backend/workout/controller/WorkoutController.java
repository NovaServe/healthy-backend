package healthy.lifestyle.backend.workout.controller;

import healthy.lifestyle.backend.user.service.AuthUtil;
import healthy.lifestyle.backend.validation.DescriptionValidation;
import healthy.lifestyle.backend.validation.TitleValidation;
import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import healthy.lifestyle.backend.workout.service.WorkoutService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
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

    private final AuthUtil authUtil;

    public WorkoutController(WorkoutService workoutService, AuthUtil authUtil) {
        this.workoutService = workoutService;
        this.authUtil = authUtil;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> createCustomWorkout(
            @Valid @RequestBody WorkoutCreateRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
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
    public ResponseEntity<Page<WorkoutResponseDto>> getDefaultWorkouts(
            @RequestParam(required = false) @TitleValidation String title,
            @RequestParam(required = false) @DescriptionValidation String description,
            @RequestParam(required = false) Boolean needsEquipment,
            @RequestParam(required = false) List<Long> bodyPartsIds,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Page<WorkoutResponseDto> dtoPage = workoutService.getWorkoutsWithFilter(
                false,
                null,
                title,
                description,
                needsEquipment,
                bodyPartsIds,
                sortField,
                sortDirection,
                pageNumber,
                pageSize);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<WorkoutResponseDto>> getWorkouts(
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) @TitleValidation String title,
            @RequestParam(required = false) @DescriptionValidation String description,
            @RequestParam(required = false) Boolean needsEquipment,
            @RequestParam(required = false) List<Long> bodyPartsIds,
            @RequestParam(required = false, defaultValue = "title") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        Long userId = null;
        if (isCustom == null || isCustom)
            userId = authUtil.getUserIdFromAuthentication(
                    SecurityContextHolder.getContext().getAuthentication());
        Page<WorkoutResponseDto> dtoPage = workoutService.getWorkoutsWithFilter(
                isCustom,
                userId,
                title,
                description,
                needsEquipment,
                bodyPartsIds,
                sortField,
                sortDirection,
                pageNumber,
                pageSize);
        return ResponseEntity.ok(dtoPage);
    }

    @PatchMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> updateCustomWorkout(
            @PathVariable long workoutId, @Valid @RequestBody WorkoutUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutResponseDto responseDto = workoutService.updateCustomWorkout(userId, workoutId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomWorkout(@PathVariable("workoutId") Long workoutId) {
        Long authenticatedUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        workoutService.deleteCustomWorkout(authenticatedUserId, workoutId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
