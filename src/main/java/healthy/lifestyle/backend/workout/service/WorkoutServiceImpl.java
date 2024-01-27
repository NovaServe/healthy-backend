package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutServiceImpl implements WorkoutService {
    private final WorkoutRepository workoutRepository;

    private final ExerciseRepository exerciseRepository;

    private final BodyPartRepository bodyPartRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    public WorkoutServiceImpl(
            WorkoutRepository workoutRepository,
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            UserService userService,
            ModelMapper modelMapper) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public WorkoutResponseDto createCustomWorkout(long userId, WorkoutCreateRequestDto requestDto) {
        List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId);
        if (workouts.size() > 0) throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        User user = userService.getUserById(userId);
        Set<Exercise> exerciseSet = new HashSet<>();
        Set<BodyPart> workoutBodyParts = new HashSet<>();
        boolean workoutNeedsEquipment = false;

        for (long exerciseId : requestDto.getExerciseIds()) {
            Exercise exercise = exerciseRepository
                    .findById(exerciseId)
                    .orElseThrow(
                            () -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, exerciseId, HttpStatus.NOT_FOUND));

            if (exercise.isCustom() && !exercise.getUser().equals(user))
                throw new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, exerciseId, HttpStatus.BAD_REQUEST);

            exerciseSet.add(exercise);
            workoutBodyParts.addAll(exercise.getBodyParts());
            if (exercise.isNeedsEquipment()) workoutNeedsEquipment = true;
        }

        Workout workout = Workout.builder()
                .isCustom(true)
                .user(user)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .exercises(exerciseSet)
                .build();
        Workout savedWorkout = workoutRepository.save(workout);
        userService.addWorkout(user, savedWorkout);

        WorkoutResponseDto workoutResponseDto = modelMapper.map(savedWorkout, WorkoutResponseDto.class);

        List<BodyPartResponseDto> workoutBodyPartResponseDtoList = workoutBodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();

        workoutResponseDto.setBodyParts(workoutBodyPartResponseDtoList);
        workoutResponseDto.setNeedsEquipment(workoutNeedsEquipment);

        List<ExerciseResponseDto> exerciseResponseDtoList = savedWorkout.getExercisesSortedById().stream()
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
                .toList();

        workoutResponseDto.setExercises(exerciseResponseDtoList);
        return workoutResponseDto;
    }

    @Override
    @Transactional
    public WorkoutResponseDto getWorkoutById(long workoutId, boolean customRequired) {
        Workout workout = workoutRepository
                .findById(workoutId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.NOT_FOUND));

        if (workout.isCustom() && !customRequired)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!workout.isCustom() && customRequired)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        WorkoutResponseDto workoutDto = modelMapper.map(workout, WorkoutResponseDto.class);

        List<ExerciseResponseDto> exercisesSorted = workoutDto.getExercises().stream()
                .sorted(Comparator.comparingLong(ExerciseResponseDto::getId))
                .toList();

        workoutDto.setExercises(exercisesSorted);

        Set<BodyPartResponseDto> workoutBodyParts = new HashSet<>();
        boolean workoutNeedsEquipment = false;

        for (ExerciseResponseDto exercise : exercisesSorted) {
            for (BodyPartResponseDto bodyPart : exercise.getBodyParts()) {
                workoutBodyParts.add(bodyPart);
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
    public Page<WorkoutResponseDto> getWorkoutsWithFilter(
            Boolean isCustom,
            Long userId,
            String title,
            String description,
            Boolean needsEquipment,
            List<Long> bodyPartsIds,
            String sortField,
            String sortDirection,
            int currentPageNumber,
            int pageSize) {

        Pageable pageable = PageRequest.of(
                currentPageNumber, pageSize, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        if (bodyPartsIds == null || bodyPartsIds.size() == 0) {
            bodyPartsIds =
                    bodyPartRepository.findAll().stream().map(BodyPart::getId).toList();
        }

        Page<Workout> entitiesPage = null;

        // Default and custom, with equipment filter
        if (isCustom == null && userId != null && needsEquipment != null) {
            entitiesPage = workoutRepository.findDefaultAndCustomNeedsEquipmentWithFilter(
                    userId, title, description, needsEquipment, bodyPartsIds, pageable);
        }
        // Default and custom, without equipment filter
        else if (isCustom == null && userId != null && needsEquipment == null) {
            entitiesPage = workoutRepository.findDefaultAndCustomWithFilter(
                    userId, title, description, bodyPartsIds, pageable);
        }
        // Default only, with equipment filter
        else if (isCustom != null && !isCustom && userId == null && needsEquipment != null) {
            entitiesPage = workoutRepository.findDefaultOrCustomNeedsEquipmentWithFilter(
                    false, null, title, description, needsEquipment, bodyPartsIds, pageable);
        }
        // Default only, without equipment filter
        else if (isCustom != null && !isCustom && userId == null && needsEquipment == null) {
            entitiesPage = workoutRepository.findDefaultOrCustomWithFilter(
                    false, null, title, description, bodyPartsIds, pageable);
        }
        // Custom only, with equipment filter
        else if (isCustom != null && isCustom && userId != null && needsEquipment != null) {
            entitiesPage = workoutRepository.findDefaultOrCustomNeedsEquipmentWithFilter(
                    true, userId, title, description, needsEquipment, bodyPartsIds, pageable);
        }
        // Custom only, without equipment filter
        else if (isCustom != null && isCustom && userId != null && needsEquipment == null) {
            entitiesPage = workoutRepository.findDefaultOrCustomWithFilter(
                    true, userId, title, description, bodyPartsIds, pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<WorkoutResponseDto> dtoPage =
                entitiesPage.map(entity -> modelMapper.map(entity, WorkoutResponseDto.class));
        return dtoPage;
    }

    @Override
    @Transactional
    public List<WorkoutResponseDto> getWorkouts(String sortBy, boolean isDefault, Long userId) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortBy);
        List<Workout> workouts;
        if (isDefault) workouts = workoutRepository.findAllDefault(sort);
        else workouts = workoutRepository.findAllCustomByUserId(sort, userId);

        List<WorkoutResponseDto> responseDtoList = workouts.stream()
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
                            bodyParts.add(bodyPart);
                            if (exercise.isNeedsEquipment()) needsEquipment = true;
                        }
                    }

                    elt.setBodyParts(bodyParts.stream()
                            .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                            .toList());

                    elt.setNeedsEquipment(needsEquipment);
                })
                .toList();
        return responseDtoList;
    }

    @Override
    @Transactional
    public WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto) {
        if (requestDto.getTitle() == null
                && requestDto.getDescription() == null
                && requestDto.getExerciseIds().size() == 0)
            throw new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        User user = userService.getUserById(userId);
        Workout workout = workoutRepository
                .findById(workoutId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.NOT_FOUND));

        if (!workout.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);
        if (user.getWorkouts() == null || !user.getWorkouts().contains(workout))
            throw new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, workoutId, HttpStatus.BAD_REQUEST);

        if (requestDto.getTitle() != null && !requestDto.getTitle().equals(workout.getTitle())) {
            List<Workout> workouts = workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId);
            if (workouts.size() > 0) throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
            workout.setTitle(requestDto.getTitle());
        }

        if (requestDto.getDescription() != null && !requestDto.getDescription().equals(workout.getDescription()))
            workout.setDescription(requestDto.getDescription());

        boolean workoutNeedsEquipment = false;
        Set<BodyPart> workoutBodyParts = new HashSet<>();

        if (requestDto.getExerciseIds().size() > 0) {
            Set<Long> idsToAdd = new HashSet<>(requestDto.getExerciseIds());
            Set<Long> idsToRemove = new HashSet<>();

            for (Exercise exercise : workout.getExercises()) {
                idsToAdd.remove(exercise.getId());

                if (!requestDto.getExerciseIds().contains(exercise.getId())) {
                    idsToRemove.add(exercise.getId());
                }

                if (exercise.isNeedsEquipment()) workoutNeedsEquipment = true;
                workoutBodyParts.addAll(exercise.getBodyParts());
            }

            for (long id : idsToAdd) {
                Optional<Exercise> exerciseOptional = exerciseRepository.findById(id);
                if (exerciseOptional.isEmpty())
                    throw new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, id, HttpStatus.NOT_FOUND);

                Exercise exercise = exerciseOptional.get();
                if (exercise.isCustom()) {
                    boolean userHasCustomExercise =
                            user.getExercises() != null && user.getExercises().contains(exercise);
                    if (!userHasCustomExercise)
                        throw new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, id, HttpStatus.BAD_REQUEST);
                }

                workout.getExercises().add(exercise);
                if (exercise.isNeedsEquipment()) workoutNeedsEquipment = true;
                workoutBodyParts.addAll(exercise.getBodyParts());
            }

            for (long id : idsToRemove) {
                Exercise exercise = exerciseRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, id, HttpStatus.NOT_FOUND));
                workout.getExercises().remove(exercise);
                workoutBodyParts.removeAll(exercise.getBodyParts());
            }
        }

        Workout savedWorkout = workoutRepository.save(workout);
        WorkoutResponseDto workoutResponseDto = modelMapper.map(savedWorkout, WorkoutResponseDto.class);

        List<BodyPartResponseDto> workoutBodyPartResponseDtoList = workoutBodyParts.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        workoutResponseDto.setBodyParts(workoutBodyPartResponseDtoList);
        workoutResponseDto.setNeedsEquipment(workoutNeedsEquipment);

        List<ExerciseResponseDto> exerciseResponseDtoList = savedWorkout.getExercisesSortedById().stream()
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
                .toList();

        workoutResponseDto.setExercises(exerciseResponseDtoList);
        return workoutResponseDto;
    }

    @Override
    @Transactional
    public void deleteCustomWorkout(long userId, long workoutId) {
        Workout workout = workoutRepository
                .findById(workoutId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.NOT_FOUND));
        if (!workout.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);
        if (userId != workout.getUser().getId())
            throw new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, workoutId, HttpStatus.BAD_REQUEST);
        User user = userService.getUserById(userId);
        userService.removeWorkout(user, workout);
        workoutRepository.delete(workout);
    }
}
