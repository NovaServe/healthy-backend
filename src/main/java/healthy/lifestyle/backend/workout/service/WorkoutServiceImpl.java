package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
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

    private final ExerciseRepository exerciseRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    public WorkoutServiceImpl(
            WorkoutRepository workoutRepository,
            ExerciseRepository exerciseRepository,
            UserService userService,
            ModelMapper modelMapper) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userService = userService;
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

    @Override
    @Transactional
    public WorkoutResponseDto createCustomWorkout(long userId, CreateWorkoutRequestDto requestDto) {
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId);
        if (workouts.size() > 0) throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);

        User user = userService.getUserById(userId);
        Set<Exercise> exerciseSet = new HashSet<>();
        Set<BodyPart> bodyParts = new HashSet<>();
        boolean needsEquipment = false;

        for (long exerciseId : requestDto.getExerciseIds()) {
            Optional<Exercise> exerciseOptional = exerciseRepository.findById(exerciseId);
            if (exerciseOptional.isEmpty())
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);

            Exercise exercise = exerciseOptional.get();
            if (exercise.isCustom() && !exercise.getUsers().contains(user))
                throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

            exerciseSet.add(exercise);
            bodyParts.addAll(exercise.getBodyParts());
            if (exercise.isNeedsEquipment()) needsEquipment = true;
        }

        Workout workout = Workout.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .isCustom(true)
                .exercises(exerciseSet)
                .build();

        Workout saved = workoutRepository.save(workout);
        userService.addWorkout(userId, workout);

        WorkoutResponseDto workoutResponseDto = modelMapper.map(saved, WorkoutResponseDto.class);
        List<BodyPartResponseDto> bodyPartRequestDtoList = bodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        workoutResponseDto.setBodyParts(bodyPartRequestDtoList);
        workoutResponseDto.setNeedsEquipment(needsEquipment);

        List<ExerciseResponseDto> exerciseResponseDtoList = saved.getExercises().stream()
                .map(exercise -> modelMapper.map(exercise, ExerciseResponseDto.class))
                .peek(elt -> {
                    List<BodyPartResponseDto> bodyPartsSorted = elt.getBodyParts().stream()
                            .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                            .toList();

                    List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
                            .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                            .toList();

                    elt.setBodyParts(bodyPartsSorted);
                    elt.setHttpRefs(httpRefsSorted);
                })
                .sorted(Comparator.comparingLong(ExerciseResponseDto::getId))
                .toList();

        workoutResponseDto.setExercises(exerciseResponseDtoList);
        return workoutResponseDto;
    }
}
