package healthy.lifestyle.backend.activity.workout.service;

import healthy.lifestyle.backend.activity.workout.dto.*;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.activity.workout.repository.WorkoutRepository;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.util.VerificationUtil;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkoutServiceImpl implements WorkoutService {
    @Autowired
    WorkoutRepository workoutRepository;

    @Autowired
    ExerciseRepository exerciseRepository;

    @Autowired
    BodyPartRepository bodyPartRepository;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    VerificationUtil verificationUtil;

    @Override
    @Transactional
    public WorkoutResponseDto createCustomWorkout(long userId, WorkoutCreateRequestDto requestDto) {
        List<Workout> workoutsWithSameTitle =
                workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);

        if (!workoutsWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

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
        userService.addWorkoutToUser(user, savedWorkout);

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

        Page<WorkoutResponseDto> dtoPage = entitiesPage.map(entity -> {
            WorkoutResponseDto workoutResponseDto = modelMapper.map(entity, WorkoutResponseDto.class);
            boolean workoutNeedsEquipment = false;
            for (Exercise exercise : entity.getExercises()) {
                if (exercise.isNeedsEquipment()) {
                    workoutNeedsEquipment = true;
                    break;
                }
            }
            workoutResponseDto.setNeedsEquipment(workoutNeedsEquipment);
            return workoutResponseDto;
        });
        return dtoPage;
    }

    @Override
    @Transactional
    public WorkoutResponseDto updateCustomWorkout(long userId, long workoutId, WorkoutUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {

        Workout workout = workoutRepository
                .findById(workoutId)
                .orElseThrow(() -> new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, workoutId, HttpStatus.NOT_FOUND));

        if (!workout.isCustom()) {
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);
        }

        if (userId != workout.getUser().getId()) {
            throw new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, workoutId, HttpStatus.BAD_REQUEST);
        }

        boolean fieldsAreNull = verificationUtil.areFieldsNull(requestDto, "title", "description");
        boolean exercisesAreDifferent = verificationUtil.areNestedEntitiesDifferent(
                workout.getSortedExercisesIds(), requestDto.getExerciseIds());
        if (fieldsAreNull && !exercisesAreDifferent) {
            throw new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
        }

        List<String> fieldsWithSameValues =
                verificationUtil.getFieldsWithSameValues(workout, requestDto, "title", "description");
        if (!fieldsWithSameValues.isEmpty()) {
            String errorMessage =
                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + String.join(", ", fieldsWithSameValues);
            throw new ApiExceptionCustomMessage(errorMessage, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getTitle() != null) {
            List<Workout> workoutsWithSameTitle =
                    workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);
            if (!workoutsWithSameTitle.isEmpty()) {
                throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
            }
            workout.setTitle(requestDto.getTitle());
        }

        if (requestDto.getDescription() != null) {
            workout.setDescription(requestDto.getDescription());
        }

        if (exercisesAreDifferent) {
            updateExercises(workout, requestDto.getExerciseIds());
        }

        Workout savedWorkout = workoutRepository.save(workout);
        WorkoutResponseDto workoutResponseDto = mapWorkoutToWorkoutResponseDto(savedWorkout);
        return workoutResponseDto;
    }

    private void updateExercises(Workout workout, List<Long> exercisesIds) {
        if (exercisesIds == null || exercisesIds.isEmpty()) {
            throw new ApiExceptionCustomMessage(
                    ErrorMessage.WORKOUT_SHOULD_HAVE_EXERCISES.getName(), HttpStatus.BAD_REQUEST);
        }

        Set<Long> idsToAdd = new HashSet<>(exercisesIds);
        Set<Long> idsToRemove = new HashSet<>();
        for (Exercise exercise : workout.getExercises()) {
            idsToAdd.remove(exercise.getId());
            if (!exercisesIds.contains(exercise.getId())) {
                idsToRemove.add(exercise.getId());
            }
        }

        for (long id : idsToAdd) {
            Exercise exercise = exerciseRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, id, HttpStatus.NOT_FOUND));

            if (exercise.isCustom() && !workout.getUser().equals(exercise.getUser())) {
                throw new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, id, HttpStatus.BAD_REQUEST);
            }
            workout.getExercises().add(exercise);
        }

        for (long id : idsToRemove) {
            Exercise exercise = exerciseRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, id, HttpStatus.NOT_FOUND));
            workout.getExercises().remove(exercise);
        }
    }

    private WorkoutResponseDto mapWorkoutToWorkoutResponseDto(Workout workout) {
        boolean workoutNeedsEquipment = false;
        for (Exercise exercise : workout.getExercises()) {
            if (exercise.isNeedsEquipment()) {
                workoutNeedsEquipment = true;
                break;
            }
        }

        WorkoutResponseDto workoutResponseDto = modelMapper.map(workout, WorkoutResponseDto.class);

        List<BodyPartResponseDto> workoutBodyPartResponseDtoList = workout.getDistinctBodyPartsSortedById().stream()
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        workoutResponseDto.setBodyParts(workoutBodyPartResponseDtoList);
        workoutResponseDto.setNeedsEquipment(workoutNeedsEquipment);

        List<ExerciseResponseDto> exerciseResponseDtoList = workout.getExercisesSortedById().stream()
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
        userService.deleteWorkoutFromUser(user, workout);
        workoutRepository.delete(workout);
    }
}
