package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import java.util.List;

public interface WorkoutService {
    WorkoutResponseDto createCustomWorkout(long userId, WorkoutCreateRequestDto requestDto);

    WorkoutResponseDto getWorkoutById(long workoutId, boolean customRequired);

    List<WorkoutResponseDto> getDefaultWorkouts(String sortFieldName);

    WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto);

    void deleteCustomWorkout(long userId, long workoutId);
}
