package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.ExerciseCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import java.util.List;

public interface ExerciseService {
    ExerciseResponseDto createExercise(ExerciseCreateRequestDto requestDto, long userId);

    ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId);

    List<ExerciseResponseDto> getDefaultExercises();

    List<ExerciseResponseDto> getCustomExercises(long userId);

    ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto);

    void deleteCustomExercise(long exerciseId, long userId);
}
