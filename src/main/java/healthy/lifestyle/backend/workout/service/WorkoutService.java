package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface WorkoutService {
    WorkoutResponseDto createCustomWorkout(long userId, WorkoutCreateRequestDto requestDto);

    WorkoutResponseDto getWorkoutById(long workoutId, boolean customRequired);

    Page<WorkoutResponseDto> getWorkoutsWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Boolean needsEquipment,
            List<Long> bodyPartsIds,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize);

    List<WorkoutResponseDto> getWorkouts(String sortBy, boolean isDefault, Long userId);

    WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto);

    void deleteCustomWorkout(long userId, long workoutId);
}
