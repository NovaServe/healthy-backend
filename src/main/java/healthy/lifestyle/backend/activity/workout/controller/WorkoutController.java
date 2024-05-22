package healthy.lifestyle.backend.activity.workout.controller;

import healthy.lifestyle.backend.activity.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.activity.workout.dto.WorkoutUpdateRequestDto;
import healthy.lifestyle.backend.activity.workout.service.WorkoutService;
import healthy.lifestyle.backend.shared.validation.annotation.DescriptionOptionalValidation;
import healthy.lifestyle.backend.shared.validation.annotation.IdValidation;
import healthy.lifestyle.backend.shared.validation.annotation.TitleOptionalValidation;
import healthy.lifestyle.backend.user.service.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/workouts")
public class WorkoutController {
    @Autowired
    WorkoutService workoutService;

    @Autowired
    AuthUtil authUtil;

    @Operation(summary = "Create custom workout")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> createCustomWorkout(
            @Valid @RequestBody WorkoutCreateRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutResponseDto responseDto = workoutService.createCustomWorkout(userId, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Get default workout by id")
    @GetMapping("/default/{workout_id}")
    public ResponseEntity<WorkoutResponseDto> getDefaultWorkoutById(
            @PathVariable("workout_id") @IdValidation long workoutId) {
        WorkoutResponseDto responseDto = workoutService.getWorkoutById(workoutId, false);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get custom workout by id")
    @GetMapping("/{workout_id}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutResponseDto> getCustomWorkoutById(
            @PathVariable("workout_id") @IdValidation long workoutId) {
        WorkoutResponseDto responseDto = workoutService.getWorkoutById(workoutId, true);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Get default workouts")
    @GetMapping("/default")
    public ResponseEntity<Page<WorkoutResponseDto>> getDefaultWorkouts(
            @RequestParam(required = false) @TitleOptionalValidation(min = 2) String title,
            @RequestParam(required = false) @DescriptionOptionalValidation(min = 2) String description,
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

    @Operation(summary = "Get default and custom workouts")
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<WorkoutResponseDto>> getWorkouts(
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) @TitleOptionalValidation(min = 2) String title,
            @RequestParam(required = false) @DescriptionOptionalValidation(min = 2) String description,
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

    @Operation(summary = "Update custom workout by id")
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

    @Operation(summary = "Delete custom workout by id")
    @DeleteMapping("/{workoutId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> deleteCustomWorkout(@PathVariable("workoutId") @IdValidation long workoutId) {
        Long authenticatedUserId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        workoutService.deleteCustomWorkout(authenticatedUserId, workoutId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
