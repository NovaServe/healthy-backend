package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.workout.dto.CreateExerciseRequestDto;
import healthy.lifestyle.backend.workout.dto.CreateExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.GetExercisesResponseDto;

public interface ExerciseService {
    CreateExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, Long userId);

    GetExercisesResponseDto getExercises(long userId, boolean isCustomOnly);
}
