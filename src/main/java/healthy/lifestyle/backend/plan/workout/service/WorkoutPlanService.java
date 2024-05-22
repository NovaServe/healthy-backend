package healthy.lifestyle.backend.plan.workout.service;

import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;

public interface WorkoutPlanService {
    WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId);
}
