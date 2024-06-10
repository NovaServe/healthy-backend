package healthy.lifestyle.backend.plan.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;

public interface WorkoutPlanService {
    WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId)
            throws JsonProcessingException;
}
