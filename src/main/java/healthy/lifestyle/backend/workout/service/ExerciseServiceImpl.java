package healthy.lifestyle.backend.workout.service;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ApiExceptionCustomMessage;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.Util;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.service.UserService;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
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
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final BodyPartRepository bodyPartRepository;
    private final HttpRefRepository httpRefRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final Util util;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            HttpRefRepository httpRefRepository,
            UserService userService,
            ModelMapper modelMapper,
            Util util) {
        this.exerciseRepository = exerciseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.httpRefRepository = httpRefRepository;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.util = util;
    }

    @Transactional
    @Override
    public ExerciseResponseDto createCustomExercise(ExerciseCreateRequestDto requestDto, long userId) {
        List<Exercise> exercisesWithSameTitle =
                exerciseRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);
        if (!exercisesWithSameTitle.isEmpty()) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
        }

        User user = userService.getUserById(userId);
        Exercise exercise = Exercise.builder()
                .isCustom(true)
                .user(user)
                .title(requestDto.getTitle())
                .needsEquipment(requestDto.isNeedsEquipment())
                .bodyParts(new HashSet<>())
                .httpRefs(new HashSet<>())
                .build();

        if (requestDto.getDescription() != null) exercise.setDescription(requestDto.getDescription());

        if (requestDto.getBodyParts() != null && requestDto.getBodyParts().size() > 0)
            requestDto.getBodyParts().forEach(id -> {
                BodyPart bodyPart = bodyPartRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ApiException(ErrorMessage.BODY_PART_NOT_FOUND, id, HttpStatus.NOT_FOUND));
                exercise.getBodyParts().add(bodyPart);
            });

        if (requestDto.getHttpRefs() != null && requestDto.getHttpRefs().size() > 0)
            requestDto.getHttpRefs().forEach(id -> {
                HttpRef httpRef = httpRefRepository
                        .findById(id)
                        .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));

                if (httpRef.isCustom() && httpRef.getUser().getId() != userId)
                    throw new ApiException(
                            ErrorMessage.USER_HTTP_REF_MISMATCH, httpRef.getId(), HttpStatus.BAD_REQUEST);

                exercise.getHttpRefs().add(httpRef);
            });

        Exercise exerciseSaved = exerciseRepository.save(exercise);
        userService.addExerciseToUser(userId, exerciseSaved);
        ExerciseResponseDto exerciseResponseDto = modelMapper.map(exerciseSaved, ExerciseResponseDto.class);

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

    @Override
    @Transactional
    public ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId) {
        Exercise exercise = exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, exerciseId, HttpStatus.NOT_FOUND));

        if (exercise.isCustom() && requiredDefault)
            throw new ApiException(
                    ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        if (!exercise.isCustom() && !requiredDefault)
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        if (userId != null) {
            User user = userService.getUserById(userId);
            if (exercise.isCustom()
                    && (user.getExercises() == null || !user.getExercises().contains(exercise)))
                throw new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, exerciseId, HttpStatus.BAD_REQUEST);
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
    public Page<ExerciseResponseDto> getExercisesWithFilter(
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

        Page<Exercise> entitiesPage = null;

        // Default and custom
        if (isCustom == null && userId != null) {
            entitiesPage = exerciseRepository.findDefaultAndCustomWithFilter(
                    userId, title, description, needsEquipment, bodyPartsIds, pageable);
        }
        // Default only
        else if (isCustom != null && !isCustom && userId == null) {
            entitiesPage = exerciseRepository.findDefaultOrCustomWithFilter(
                    false, null, title, description, needsEquipment, bodyPartsIds, pageable);
        }
        // Custom only
        else if (isCustom != null && isCustom && userId != null) {
            entitiesPage = exerciseRepository.findDefaultOrCustomWithFilter(
                    true, userId, title, description, needsEquipment, bodyPartsIds, pageable);
        } else {
            throw new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
        }

        Page<ExerciseResponseDto> dtoPage =
                entitiesPage.map(entity -> modelMapper.map(entity, ExerciseResponseDto.class));
        return dtoPage;
    }

    @Override
    @Transactional
    public ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto)
            throws NoSuchFieldException, IllegalAccessException {

        Exercise exercise = exerciseRepository
                .findById(exerciseId)
                .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, exerciseId, HttpStatus.NOT_FOUND));

        if (!exercise.isCustom())
            throw new ApiException(
                    ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        if (userId != exercise.getUser().getId())
            throw new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, exerciseId, HttpStatus.BAD_REQUEST);

        boolean allFieldsAreNull =
                util.verifyThatAllFieldsAreNull(requestDto, "title", "description", "needsEquipment");
        boolean bodyPartsAreDifferent = util.verifyThatNestedEntitiesAreDifferent(
                exercise.getBodyPartsIdsSorted(), requestDto.getBodyPartIds());
        boolean httpRefsAreDifferent =
                util.verifyThatNestedEntitiesAreDifferent(exercise.getHttpRefsIdsSorted(), requestDto.getHttpRefIds());
        if (allFieldsAreNull && !bodyPartsAreDifferent && !httpRefsAreDifferent) {
            throw new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
        }

        List<String> fieldsWithSameValues =
                util.verifyThatFieldsAreDifferent(exercise, requestDto, "title", "description", "needsEquipment");
        if (!fieldsWithSameValues.isEmpty()) {
            String errorMessage =
                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + String.join(", ", fieldsWithSameValues);
            throw new ApiExceptionCustomMessage(errorMessage, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getTitle() != null) {
            List<Exercise> exercisesWithSameTitle =
                    exerciseRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), userId);
            if (!exercisesWithSameTitle.isEmpty()) {
                throw new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
            }
            exercise.setTitle(requestDto.getTitle());
        }

        if (requestDto.getDescription() != null) {
            exercise.setDescription(requestDto.getDescription());
        }

        if (requestDto.getNeedsEquipment() != null) {
            exercise.setNeedsEquipment(requestDto.getNeedsEquipment());
        }

        if (bodyPartsAreDifferent) updateBodyParts(requestDto, exercise);
        if (httpRefsAreDifferent) updateHttpRefs(requestDto, exercise, userId);

        Exercise savedExercise = exerciseRepository.save(exercise);
        ExerciseResponseDto responseDto = mapExerciseToExerciseResponseDto(savedExercise);
        return responseDto;
    }

    private void updateBodyParts(ExerciseUpdateRequestDto requestDto, Exercise exercise) {
        Set<Long> idsToAdd = new HashSet<>(requestDto.getBodyPartIds());
        Set<Long> idsToRemove = new HashSet<>();

        for (BodyPart bodyPart : exercise.getBodyParts()) {
            idsToAdd.remove(bodyPart.getId());
            if (!requestDto.getBodyPartIds().contains(bodyPart.getId())) idsToRemove.add(bodyPart.getId());
        }

        for (long id : idsToAdd) {
            BodyPart bodyPart = bodyPartRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.BODY_PART_NOT_FOUND, id, HttpStatus.NOT_FOUND));
            exercise.getBodyParts().add(bodyPart);
        }

        for (long id : idsToRemove) {
            BodyPart bodyPart = bodyPartRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.BODY_PART_NOT_FOUND, id, HttpStatus.NOT_FOUND));
            exercise.getBodyParts().remove(bodyPart);
        }
    }

    private void updateHttpRefs(ExerciseUpdateRequestDto requestDto, Exercise exercise, Long userId) {
        User user = userService.getUserById(userId);

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
            HttpRef httpRef = httpRefRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));

            if (httpRef.isCustom()) {
                boolean userHasCustomHttpRef =
                        user.getHttpRefs() != null && user.getHttpRefs().contains(httpRef);
                if (!userHasCustomHttpRef)
                    throw new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, id, HttpStatus.BAD_REQUEST);
            }

            exercise.getHttpRefs().add(httpRef);
        }

        for (long id : idsToRemove) {
            HttpRef httpRef = httpRefRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, id, HttpStatus.NOT_FOUND));
            exercise.getHttpRefs().remove(httpRef);
        }
    }

    private ExerciseResponseDto mapExerciseToExerciseResponseDto(Exercise exercise) {
        ExerciseResponseDto exerciseResponseDto = modelMapper.map(exercise, ExerciseResponseDto.class);

        List<BodyPartResponseDto> exerciseBodyPartsSorted = exercise.getBodyPartsSortedById().stream()
                .map(bodyPart -> modelMapper.map(bodyPart, BodyPartResponseDto.class))
                .toList();
        List<HttpRefResponseDto> exerciseHttpRefsSorted = exercise.getHttpRefsSortedById().stream()
                .map(httpRef -> modelMapper.map(httpRef, HttpRefResponseDto.class))
                .toList();

        exerciseResponseDto.setBodyParts(exerciseBodyPartsSorted);
        exerciseResponseDto.setHttpRefs(exerciseHttpRefsSorted);
        return exerciseResponseDto;
    }

    @Override
    @Transactional
    public void deleteCustomExercise(long exerciseId, long userId) {
        Exercise exercise = exerciseRepository
                .findCustomByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, exerciseId, HttpStatus.NOT_FOUND));
        userService.deleteExerciseFromUser(userId, exercise);
        exerciseRepository.delete(exercise);
    }
}
