package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import java.util.List;

public interface ExerciseService {
    ExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, long userId);

    List<ExerciseResponseDto> getCustomExercises(long userId);

    List<ExerciseResponseDto> getDefaultExercises();
}
