package healthy.lifestyle.backend.plan.workout.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutWithoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.service.WorkoutPlanService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar/workouts")
public class WorkoutPlanController {
    @Autowired
    AuthUtil authUtil;

    @Autowired
    WorkoutPlanService workoutPlanService;

    @PostMapping("/plans")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Create workout plan (add workout to the calendar)")
    public ResponseEntity<WorkoutPlanResponseDto> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanCreateRequestDto requestDto) throws JsonProcessingException {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutPlanResponseDto responseDto = workoutPlanService.createWorkoutPlan(requestDto, userId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get a list of default and custom workouts without plans")
    public ResponseEntity<List<WorkoutWithoutPlanResponseDto>> getDefaultAndCustomWorkoutsWithoutPlans() {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        List<WorkoutWithoutPlanResponseDto> responseDto =
                workoutPlanService.getDefaultAndCustomWorkoutsWithoutPlans(userId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping("/plans")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Operation(summary = "Get active workout plans")
    public ResponseEntity<List<WorkoutPlanResponseDto>> getWorkoutPlans() {
        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        List<WorkoutPlanResponseDto> responseDto = workoutPlanService.getWorkoutPlans(userId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
