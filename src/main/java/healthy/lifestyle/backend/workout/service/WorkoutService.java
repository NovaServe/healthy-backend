package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import java.util.List;

public interface WorkoutService {
    List<WorkoutResponseDto> getDefaultWorkouts(String sortFieldName);

    WorkoutResponseDto getWorkoutById(long workoutId);
}
