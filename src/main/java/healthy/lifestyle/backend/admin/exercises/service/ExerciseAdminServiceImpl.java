package healthy.lifestyle.backend.admin.exercises.service;

import healthy.lifestyle.backend.admin.exercises.repository.ExerciseAdminRepository;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Comparator;

@Service
public class ExerciseAdminServiceImpl implements ExerciseAdminService{
    private final ExerciseAdminRepository exerciseAdminRepository;

    private final ModelMapper modelMapper;

    public ExerciseAdminServiceImpl(
            ExerciseAdminRepository exerciseAdminRepository,
            ModelMapper modelMapper){
        this.exerciseAdminRepository = exerciseAdminRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ExerciseResponseDto> getExercisesByFilters(String title, String description,
                                                          boolean isCustom, boolean needsEquipment){

        List<Exercise> exercises = exerciseAdminRepository.findByFilters(title, description, isCustom, needsEquipment)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));

        return exercises.stream()
                .map(exercise -> modelMapper.map(exercise, ExerciseResponseDto.class))
                .sorted(Comparator.comparing(ExerciseResponseDto::getId))
                .toList();
    }

}
