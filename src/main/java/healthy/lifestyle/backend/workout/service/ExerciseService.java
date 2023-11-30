package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import java.util.List;

public interface ExerciseService {
    ExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, long userId);

    List<ExerciseResponseDto> getCustomExercises(long userId);

    List<ExerciseResponseDto> getDefaultExercises();

    ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId);

    ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto);

    Long deleteCustomExercise(long exerciseId, long userId);
}
