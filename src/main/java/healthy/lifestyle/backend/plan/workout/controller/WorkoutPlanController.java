package healthy.lifestyle.backend.plan.workout.controller;

import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.service.WorkoutPlanService;
import healthy.lifestyle.backend.user.service.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Controller
@RequestMapping("${api.basePath}/${api.version}/calendar/workouts")
public class WorkoutPlanController {

    @Autowired
    AuthUtil authUtil;

    @Autowired
    WorkoutPlanService workoutPlanService;

    @PostMapping("/plans")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<WorkoutPlanResponseDto> createWorkoutPlan(
            @Valid @RequestBody WorkoutPlanCreateRequestDto requestDto) {

        Long userId = authUtil.getUserIdFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());
        WorkoutPlanResponseDto responseDto = workoutPlanService.createWorkoutPlan(requestDto, userId);

        // call update tasks
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}
