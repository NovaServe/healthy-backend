package healthy.lifestyle.backend.admin.exercises.service;

import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import java.util.List;

public interface ExerciseAdminService {
    List<ExerciseResponseDto> getExercisesByFilters(String title, String description,
                                                    boolean isCustom, boolean needsEquipment);
}
