package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateWorkoutRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import java.util.List;

public interface WorkoutService {
    List<WorkoutResponseDto> getDefaultWorkouts(String sortFieldName);

    WorkoutResponseDto getDefaultWorkoutById(long workoutId);

    WorkoutResponseDto createCustomWorkout(long userId, CreateWorkoutRequestDto requestDto);
}
