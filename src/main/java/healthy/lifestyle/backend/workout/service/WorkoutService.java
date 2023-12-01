package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import java.util.List;

public interface WorkoutService {
    List<WorkoutResponseDto> getDefaultWorkouts(String sortFieldName);

    WorkoutResponseDto getWorkoutById(long workoutId, boolean customRequired);

    WorkoutResponseDto createCustomWorkout(long userId, WorkoutCreateRequestDto requestDto);

    WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto);

    long deleteCustomWorkout(long userId, long workoutId);
}
