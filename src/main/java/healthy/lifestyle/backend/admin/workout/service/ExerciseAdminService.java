package healthy.lifestyle.backend.admin.workout.service;

import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import java.util.List;

public interface ExerciseAdminService {
    List<ExerciseResponseDto> getExercisesByFilters(
            String title, String description, Boolean isCustom, Boolean needsEquipment);
}
