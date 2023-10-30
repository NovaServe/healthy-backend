package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
        Optional<Workout> workoutOptional = workoutRepository.findById(id);

        if (workoutOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND);

        if (workoutOptional.isPresent() && workoutOptional.get().isCustom())
            throw new ApiException(ErrorMessage.UNAUTHORIZED_FOR_THIS_RESOURCE, HttpStatus.UNAUTHORIZED);

        WorkoutResponseDto workoutDto = modelMapper.map(workoutOptional.get(), WorkoutResponseDto.class);

        List<ExerciseResponseDto> exercisesSorted = workoutDto.getExercises().stream()
                .sorted(Comparator.comparingLong(ExerciseResponseDto::getId))
                .toList();

        workoutDto.setExercises(exercisesSorted);

        Set<BodyPartResponseDto> workoutBodyParts = new HashSet<>();
        boolean workoutNeedsEquipment = false;

        for (ExerciseResponseDto exercise : exercisesSorted) {
            for (BodyPartResponseDto bodyPart : exercise.getBodyParts()) {
                if (!workoutBodyParts.contains(bodyPart)) workoutBodyParts.add(bodyPart);
                if (exercise.isNeedsEquipment()) workoutNeedsEquipment = true;
            }
        }

        workoutDto.setBodyParts(workoutBodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList());

        workoutDto.setNeedsEquipment(workoutNeedsEquipment);

        return workoutDto;
    }
}
