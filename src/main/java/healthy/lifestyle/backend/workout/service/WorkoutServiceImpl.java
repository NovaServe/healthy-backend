package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutServiceImpl implements WorkoutService {
    private final WorkoutRepository workoutRepository;

    private final ModelMapper modelMapper;

    public WorkoutServiceImpl(WorkoutRepository workoutRepository, ModelMapper modelMapper) {
        this.workoutRepository = workoutRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public List<WorkoutResponseDto> getDefaultWorkouts(String sortFieldName) {
        if (isNull(sortFieldName)) sortFieldName = "id";

        return workoutRepository.findAllDefault(Sort.by(Sort.Direction.ASC, sortFieldName)).stream()
                .map(workout -> modelMapper.map(workout, WorkoutResponseDto.class))
                .peek(elt -> {
                    List<ExerciseResponseDto> exercisesSorted = elt.getExercises().stream()
                            .sorted(Comparator.comparingLong(ExerciseResponseDto::getId))
                            .toList();

                    elt.setExercises(exercisesSorted);

                    Set<BodyPartResponseDto> bodyParts = new HashSet<>();
                    boolean needsEquipment = false;

                    for (ExerciseResponseDto exercise : exercisesSorted) {
                        for (BodyPartResponseDto bodyPart : exercise.getBodyParts()) {
                            if (!bodyParts.contains(bodyPart)) bodyParts.add(bodyPart);
                            if (exercise.isNeedsEquipment()) needsEquipment = true;
                        }
                    }

                    elt.setBodyParts(bodyParts.stream()
                            .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                            .toList());

                    elt.setNeedsEquipment(needsEquipment);
                })
                .toList();
    }

    @Transactional
    @Override
    public WorkoutResponseDto getDefaultWorkoutById(long id) {
        WorkoutResponseDto workoutDto = modelMapper.map(workoutRepository.findById(id), WorkoutResponseDto.class);

        if (workoutDto == null) throw new EntityNotFoundException("Workout not found");
        if (workoutDto.isCustom()) throw new RuntimeException("Access to custom workout is unauthorized");

        List<ExerciseResponseDto> exercisesSorted = workoutDto.getExercises().stream()
                .sorted(Comparator.comparingLong(ExerciseResponseDto::getId))
                .toList();

        workoutDto.setExercises(exercisesSorted);

        Set<BodyPartResponseDto> bodyParts = new HashSet<>();
        boolean needsEquipment = false;

        for (ExerciseResponseDto exercise : exercisesSorted) {
            for (BodyPartResponseDto bodyPart : exercise.getBodyParts()) {
                if (!bodyParts.contains(bodyPart)) bodyParts.add(bodyPart);
                if (exercise.isNeedsEquipment()) needsEquipment = true;
            }
        }

        workoutDto.setBodyParts(bodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList());

        workoutDto.setNeedsEquipment(needsEquipment);
        return workoutDto;
    }
}
