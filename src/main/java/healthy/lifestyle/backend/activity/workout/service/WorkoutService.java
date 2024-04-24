package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.activity.workout.dto.WorkoutUpdateRequestDto;
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

    WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException;

    void deleteCustomWorkout(long userId, long workoutId);
}
