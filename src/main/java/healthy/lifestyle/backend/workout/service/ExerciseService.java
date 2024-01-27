package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.ExerciseCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ExerciseService {
    ExerciseResponseDto createCustomExercise(ExerciseCreateRequestDto requestDto, long userId);

    ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId);

    Page<ExerciseResponseDto> getExercisesWithFilter(
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

    List<ExerciseResponseDto> getDefaultExercises();

    List<ExerciseResponseDto> getCustomExercises(long userId);

    ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto);

    void deleteCustomExercise(long exerciseId, long userId);
}
