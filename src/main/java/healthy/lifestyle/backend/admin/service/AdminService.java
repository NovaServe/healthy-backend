package healthy.lifestyle.backend.admin.service;

import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;

import java.util.List;

public interface AdminService {
    List<ExerciseResponseDto> getCustomExercises();
}
