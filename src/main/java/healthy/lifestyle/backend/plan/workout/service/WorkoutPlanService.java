package healthy.lifestyle.backend.plan.workout.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutWithoutPlanResponseDto;
import java.util.List;

public interface WorkoutPlanService {
    WorkoutPlanResponseDto createWorkoutPlan(WorkoutPlanCreateRequestDto requestDto, long userId)
            throws JsonProcessingException;

    List<WorkoutWithoutPlanResponseDto> getDefaultAndCustomWorkoutsWithoutPlans(long userId);

    List<WorkoutPlanResponseDto> getWorkoutPlans(long userId);
}
