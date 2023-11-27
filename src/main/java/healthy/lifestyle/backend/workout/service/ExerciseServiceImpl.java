package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.exception.ExceptionGeneric;
import healthy.lifestyle.backend.exception.ExceptionHandler;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserService;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseRepository exerciseRepository;

    private final BodyPartRepository bodyPartRepository;

    private final HttpRefRepository httpRefRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            HttpRefRepository httpRefRepository,
            UserService userService,
            ModelMapper modelMapper) {
        this.exerciseRepository = exerciseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.httpRefRepository = httpRefRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public ExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, long userId) {
        validateCreateExerciseRequestDto(requestDto, userId);

        Exercise exercise = Exercise.builder()
                .title(requestDto.getTitle())
                .isCustom(true)
                .needsEquipment(requestDto.isNeedsEquipment())
                .bodyParts(new HashSet<>())
                .httpRefs(new HashSet<>())
                .build();

        if (nonNull(requestDto.getDescription())) exercise.setDescription(requestDto.getDescription());

        if (nonNull(requestDto.getBodyParts()) && requestDto.getBodyParts().size() > 0) {
            requestDto.getBodyParts().forEach(id -> exercise.getBodyParts()
                    .add(bodyPartRepository.getReferenceById(id)));
        }

        if (nonNull(requestDto.getHttpRefs()) && requestDto.getHttpRefs().size() > 0) {
            requestDto.getHttpRefs().forEach(id -> exercise.getHttpRefs().add(httpRefRepository.getReferenceById(id)));
        }

        Exercise saved = exerciseRepository.save(exercise);

        userService.addExercise(userId, exercise);

        ExerciseResponseDto exerciseResponseDto = modelMapper.map(saved, ExerciseResponseDto.class);

        List<BodyPartResponseDto> bodyPartsSorted = exerciseResponseDto.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList();

        List<HttpRefResponseDto> httpRefsSorted = exerciseResponseDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();

        exerciseResponseDto.setBodyParts(bodyPartsSorted);
        exerciseResponseDto.setHttpRefs(httpRefsSorted);

        return exerciseResponseDto;
    }

    private void validateCreateExerciseRequestDto(CreateExerciseRequestDto requestDto, long userId) {
        // Check is there duplicated exercise in the database for this particular user
        if (exerciseTitleDuplicateExists(requestDto.getTitle(), userId))
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);

        // Check if body parts exist in the database
        if (nonNull(requestDto.getBodyParts()) && requestDto.getBodyParts().size() > 0) {
            if (!bodyPartsExist(requestDto.getBodyParts()))
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
        } else throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);

        // Check if http refs exist in the database
        if (nonNull(requestDto.getHttpRefs())) {
            if (!httpRefExists(requestDto.getHttpRefs()))
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Checks if user already has an exercise with the given title.
     * Returns true if there is no duplicates for this particular user, otherwise false.
     * There may be duplicated exercises with same title in the database table,
     * because user can have own exercise with same title as app's pre-defined exercise.
     * However, user can have only one own exercise with certain title.<br>
     * Example: there is, let say, app's pre-defined exercise "Push-up" which user can choose.
     * At the same time, user can add own "Push-up" exercise with another description, refs and body parts.
     * Therefore, there are two exercises in database with same title "Push-up". However, user can have
     * only one own exercise "Push-up". Another user can have they own exercise "Push-up" in the same database table.
     */
    private boolean exerciseTitleDuplicateExists(String exerciseTitle, long userId) {
        return exerciseRepository
                .findCustomByTitleAndUserId(exerciseTitle, userId)
                .isPresent();
    }

    /**
     * Returns true if all objects exist, otherwise false.
     */
    private boolean bodyPartsExist(List<Long> bodyPartIds) {
        for (long id : bodyPartIds) {
            if (!bodyPartRepository.existsById(id)) return false;
        }
        return true;
    }

    /**
     * Returns true if all objects exist, otherwise false.
     */
    private boolean httpRefExists(List<Long> httpRefIds) {
        for (long id : httpRefIds) {
            if (!httpRefRepository.existsById(id)) return false;
        }
        return true;
    }

    @Override
    @Transactional
    public List<ExerciseResponseDto> getCustomExercises(long userId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Exercise> exercises = exerciseRepository.findCustomByUserId(userId, sort);

        List<ExerciseResponseDto> exercisesResponseDto = exercises.stream()
                .map(elt -> modelMapper.map(elt, ExerciseResponseDto.class))
                .toList();

        return exercisesResponseDto.stream()
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
    }

    @Override
    @Transactional
    public List<ExerciseResponseDto> getDefaultExercises() {
        return exerciseRepository.findAllDefault(Sort.by(Sort.Direction.ASC, "id")).stream()
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
    }

    @Override
    @Transactional
    public ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId) {
        Optional<Exercise> exerciseOptional = exerciseRepository.findById(exerciseId);
        if (exerciseOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND);

        Exercise exercise = exerciseOptional.get();
        if ((exercise.isCustom() && requiredDefault) || (!exercise.isCustom() && !requiredDefault))
            throw new ApiException(ErrorMessage.DEFAULT_CUSTOM_MISMATCH, HttpStatus.BAD_REQUEST);

        if (nonNull(userId)) {
            User user = userService.getUserById(userId);
            if (exercise.isCustom() && !user.getExercises().contains(exercise))
                throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);
        }

        ExerciseResponseDto exerciseRespondDto = modelMapper.map(exercise, ExerciseResponseDto.class);

        List<BodyPartResponseDto> bodyPartsSorted = exerciseRespondDto.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList();

        List<HttpRefResponseDto> httpRefsSorted = exerciseRespondDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();

        exerciseRespondDto.setBodyParts(bodyPartsSorted);
        exerciseRespondDto.setHttpRefs(httpRefsSorted);

        return exerciseRespondDto;
    }

    @Override
    @Transactional
    public ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto) {
        boolean emptyRequest = isNull(requestDto.getTitle())
                && isNull(requestDto.getDescription())
                && isNull(requestDto.getNeedsEquipment())
                && requestDto.getBodyPartIds().isEmpty()
                && requestDto.getHttpRefIds().isEmpty();
        ExceptionHandler.of(ExceptionGeneric.class)
                .condition(emptyRequest)
                .messageEmptyRequest()
                .statusBadRequest()
                .throwException();

        Exercise exercise = ExceptionHandler.of(Exercise.class)
                .conditionOptional(exerciseRepository.findById(exerciseId))
                .messageNotFound()
                .statusNotFound()
                .getOrThrow();

        User user = userService.getUserById(userId);

        boolean userDoesntHaveExercise =
                isNull(user.getExercises()) || !user.getExercises().contains(exercise);
        ExceptionHandler.of(ExceptionGeneric.class)
                .condition(userDoesntHaveExercise)
                .messageUserResourceMismatch()
                .statusBadRequest()
                .throwException();

        boolean titlesAreDifferent =
                nonNull(requestDto.getTitle()) && !requestDto.getTitle().equals(exercise.getTitle());
        if (titlesAreDifferent) {
            ExceptionHandler.of(Exercise.class)
                    .conditionOptional(exerciseRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId))
                    .messageTitleDuplicate()
                    .statusBadRequest()
                    .throwIfPresent();
            exercise.setTitle(requestDto.getTitle());
        }

        boolean descriptionsAreDifferent = nonNull(requestDto.getDescription())
                && !requestDto.getDescription().equals(exercise.getDescription());
        if (descriptionsAreDifferent) exercise.setDescription(requestDto.getDescription());

        boolean needsEquipmentAreDifferent = nonNull(requestDto.getNeedsEquipment())
                && (requestDto.getNeedsEquipment() != exercise.isNeedsEquipment());
        if (needsEquipmentAreDifferent) exercise.setNeedsEquipment(requestDto.getNeedsEquipment());

        boolean bodyPartsNotEmpty = nonNull(requestDto.getBodyPartIds())
                && !requestDto.getBodyPartIds().isEmpty();
        if (bodyPartsNotEmpty) updateBodyParts(requestDto, exercise);

        if (nonNull(requestDto.getHttpRefIds())) updateMedias(requestDto, exercise, user);

        Exercise savedExercise = exerciseRepository.save(exercise);
        return mapExerciseToExerciseResponseDto(savedExercise);
    }

    private void updateBodyParts(ExerciseUpdateRequestDto requestDto, Exercise exercise) {
        Set<Long> idsToAdd = new HashSet<>(requestDto.getBodyPartIds());
        Set<Long> idsToRemove = new HashSet<>();

        for (BodyPart bodyPart : exercise.getBodyParts()) {
            idsToAdd.remove(bodyPart.getId());
            if (!requestDto.getBodyPartIds().contains(bodyPart.getId())) idsToRemove.add(bodyPart.getId());
        }

        for (long id : idsToAdd) {
            BodyPart bodyPart = ExceptionHandler.of(BodyPart.class)
                    .conditionOptional(bodyPartRepository.findById(id))
                    .messageInvalidNestedObject()
                    .statusBadRequest()
                    .getOrThrow();
            exercise.getBodyParts().add(bodyPart);
        }

        for (long id : idsToRemove) {
            BodyPart bodyPart = ExceptionHandler.of(BodyPart.class)
                    .conditionOptional(bodyPartRepository.findById(id))
                    .messageInvalidNestedObject()
                    .statusBadRequest()
                    .getOrThrow();
            exercise.getBodyParts().remove(bodyPart);
        }
    }

    private void updateMedias(ExerciseUpdateRequestDto requestDto, Exercise exercise, User user) {
        if (requestDto.getHttpRefIds().isEmpty()) {
            exercise.getHttpRefs().clear();
            return;
        }

        Set<Long> idsToAdd = new HashSet<>(requestDto.getHttpRefIds());
        Set<Long> idsToRemove = new HashSet<>();

        for (HttpRef httpRef : exercise.getHttpRefs()) {
            idsToAdd.remove(httpRef.getId());
            if (!requestDto.getHttpRefIds().contains(httpRef.getId())) idsToRemove.add(httpRef.getId());
        }

        for (long id : idsToAdd) {
            HttpRef httpRef = ExceptionHandler.of(HttpRef.class)
                    .conditionOptional(httpRefRepository.findById(id))
                    .messageInvalidNestedObject()
                    .statusBadRequest()
                    .getOrThrow();

            boolean userDoesntHaveCustomMedia =
                    httpRef.isCustom() && !user.getHttpRefs().contains(httpRef);
            ExceptionHandler.of(ExceptionGeneric.class)
                    .condition(userDoesntHaveCustomMedia)
                    .messageUserResourceMismatch()
                    .statusBadRequest()
                    .throwException();
            exercise.getHttpRefs().add(httpRef);
        }

        for (long id : idsToRemove) {
            HttpRef httpRef = ExceptionHandler.of(HttpRef.class)
                    .conditionOptional(httpRefRepository.findById(id))
                    .messageInvalidNestedObject()
                    .statusBadRequest()
                    .getOrThrow();
            exercise.getHttpRefs().remove(httpRef);
        }
    }

    private ExerciseResponseDto mapExerciseToExerciseResponseDto(Exercise exercise) {
        ExerciseResponseDto exerciseResponseDto = modelMapper.map(exercise, ExerciseResponseDto.class);

        List<BodyPartResponseDto> exerciseBodyPartsSorted = exercise.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        exerciseResponseDto.setBodyParts(exerciseBodyPartsSorted);

        List<HttpRefResponseDto> exerciseHttpRefsSorted = exercise.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class))
                .toList();
        exerciseResponseDto.setHttpRefs(exerciseHttpRefsSorted);

        return exerciseResponseDto;
    }

    @Transactional
    public ExerciseResponseDto updateCustomExerciseOld(
            long exerciseId, long userId, ExerciseUpdateRequestDto requestDto) {
        if (isNull(requestDto.getTitle())
                && isNull(requestDto.getDescription())
                && isNull(requestDto.getNeedsEquipment())
                && requestDto.getBodyPartIds().isEmpty()
                && requestDto.getHttpRefIds().isEmpty())
            throw new ApiException(ErrorMessage.EMPTY_REQUEST, HttpStatus.BAD_REQUEST);

        Exercise exercise = exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));

        User user = userService.getUserById(userId);
        if (isNull(user.getExercises()) || !user.getExercises().contains(exercise))
            throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        boolean titlesAreDifferent =
                nonNull(requestDto.getTitle()) && !requestDto.getTitle().equals(exercise.getTitle());
        if (titlesAreDifferent) {
            Optional<Exercise> exerciseOpt =
                    exerciseRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId);
            if (exerciseOpt.isPresent()) throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);
            exercise.setTitle(requestDto.getTitle());
        }

        boolean descriptionsAreDifferent = nonNull(requestDto.getDescription())
                && !requestDto.getDescription().equals(exercise.getDescription());
        if (descriptionsAreDifferent) {
            exercise.setDescription(requestDto.getDescription());
        }

        boolean needsEquipmentAreDifferent = nonNull(requestDto.getNeedsEquipment())
                && (requestDto.getNeedsEquipment() != exercise.isNeedsEquipment());
        if (needsEquipmentAreDifferent) {
            exercise.setNeedsEquipment(requestDto.getNeedsEquipment());
        }

        if (nonNull(requestDto.getBodyPartIds()) && !requestDto.getBodyPartIds().isEmpty()) {
            Set<Long> idsToAdd = new HashSet<>(requestDto.getBodyPartIds());
            Set<Long> idsToRemove = new HashSet<>();

            for (BodyPart bodyPart : exercise.getBodyParts()) {
                idsToAdd.remove(bodyPart.getId());

                if (!requestDto.getBodyPartIds().contains(bodyPart.getId())) idsToRemove.add(bodyPart.getId());
            }

            for (long id : idsToAdd) {
                Optional<BodyPart> bodyPartOptional = bodyPartRepository.findById(id);
                BodyPart bodyPart = bodyPartOptional.orElseThrow(
                        () -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
                exercise.getBodyParts().add(bodyPart);
            }

            for (long id : idsToRemove) {
                Optional<BodyPart> bodyPartOptional = bodyPartRepository.findById(id);
                BodyPart bodyPart = bodyPartOptional.get();
                exercise.getBodyParts().remove(bodyPart);
            }
        }

        if (nonNull(requestDto.getHttpRefIds()) && !requestDto.getHttpRefIds().isEmpty()) {
            Set<Long> idsToAdd = new HashSet<>(requestDto.getHttpRefIds());
            Set<Long> idsToRemove = new HashSet<>();

            for (HttpRef httpRef : exercise.getHttpRefs()) {
                idsToAdd.remove(httpRef.getId());

                if (!requestDto.getHttpRefIds().contains(httpRef.getId())) idsToRemove.add(httpRef.getId());
            }

            for (long id : idsToAdd) {
                Optional<HttpRef> httpRefOptional = httpRefRepository.findById(id);
                HttpRef httpRef = httpRefOptional.orElseThrow(
                        () -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
                if (httpRef.isCustom() && !user.getHttpRefs().contains(httpRef))
                    throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

                exercise.getHttpRefs().add(httpRef);
            }

            for (long id : idsToRemove) {
                Optional<HttpRef> httpRefOptional = httpRefRepository.findById(id);
                HttpRef httpRef = httpRefOptional.get();
                exercise.getHttpRefs().remove(httpRef);
            }
        }

        Exercise savedExercise = exerciseRepository.save(exercise);
        ExerciseResponseDto exerciseResponseDto = modelMapper.map(savedExercise, ExerciseResponseDto.class);

        List<BodyPartResponseDto> exerciseBodyPartsSorted = savedExercise.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        exerciseResponseDto.setBodyParts(exerciseBodyPartsSorted);

        List<HttpRefResponseDto> exerciseHttpRefsSorted = savedExercise.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class))
                .toList();
        exerciseResponseDto.setHttpRefs(exerciseHttpRefsSorted);

        return exerciseResponseDto;
    }
}
