package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
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
    public ExerciseResponseDto createExercise(ExerciseCreateRequestDto requestDto, long userId) {
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

    private void validateCreateExerciseRequestDto(ExerciseCreateRequestDto requestDto, long userId) {
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
    public ExerciseResponseDto getExerciseById(long exerciseId, boolean requiredDefault, Long userId) {
        Optional<Exercise> exerciseOptional = exerciseRepository.findById(exerciseId);
        if (exerciseOptional.isEmpty()) throw new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND);

        Exercise exercise = exerciseOptional.get();
        if ((exercise.isCustom() && requiredDefault) || (!exercise.isCustom() && !requiredDefault))
            throw new ApiException(ErrorMessage.DEFAULT_CUSTOM_MISMATCH, HttpStatus.BAD_REQUEST);

        if (nonNull(userId)) {
            User user = userService.getUserById(userId);
            if (exercise.isCustom()
                    && (user.getExercises() == null || !user.getExercises().contains(exercise)))
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
    public ExerciseResponseDto updateCustomExercise(long exerciseId, long userId, ExerciseUpdateRequestDto requestDto) {
        Exercise exercise = exerciseRepository
                .findCustomByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));

        boolean noUpdatesRequest = isNull(requestDto.getTitle())
                && isNull(requestDto.getDescription())
                && isNull(requestDto.getNeedsEquipment())
                && exercise.getBodyParts().stream()
                        .map(BodyPart::getId)
                        .sorted(Comparator.comparingLong(Long::longValue))
                        .toList()
                        .equals(requestDto.getBodyPartIds())
                && exercise.getHttpRefs().stream()
                        .map(HttpRef::getId)
                        .sorted(Comparator.comparingLong(Long::longValue))
                        .toList()
                        .equals(requestDto.getHttpRefIds());
        if (noUpdatesRequest) throw new ApiException(ErrorMessage.NO_UPDATES_REQUEST, HttpStatus.BAD_REQUEST);

        User user = userService.getUserById(userId);

        boolean userHasCustomExercise =
                user.getExercises() != null && user.getExercises().contains(exercise);
        if (!userHasCustomExercise) throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        if (nonNull(requestDto.getTitle())) {
            boolean titlesAreDifferent = !requestDto.getTitle().equals(exercise.getTitle());
            if (titlesAreDifferent) {
                exerciseRepository
                        .findCustomByTitleAndUserId(requestDto.getTitle(), userId)
                        .ifPresent(existingExercise -> {
                            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);
                        });
                exercise.setTitle(requestDto.getTitle());
            } else throw new ApiException(ErrorMessage.TITLES_ARE_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
        }

        if (nonNull(requestDto.getDescription())) {
            boolean descriptionsAreDifferent = !requestDto.getDescription().equals(exercise.getDescription());
            if (descriptionsAreDifferent) exercise.setDescription(requestDto.getDescription());
            else throw new ApiException(ErrorMessage.DESCRIPTIONS_ARE_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
        }

        if (nonNull(requestDto.getNeedsEquipment())) {
            boolean needsEquipmentAreDifferent = (requestDto.getNeedsEquipment() != exercise.isNeedsEquipment());
            if (needsEquipmentAreDifferent) exercise.setNeedsEquipment(requestDto.getNeedsEquipment());
            else throw new ApiException(ErrorMessage.NEEDS_EQUIPMENT_ARE_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
        }

        boolean bodyPartsAreDifferent = !exercise.getBodyParts().stream()
                .map(BodyPart::getId)
                .sorted(Comparator.comparingLong(Long::longValue))
                .toList()
                .equals(requestDto.getBodyPartIds());
        if (bodyPartsAreDifferent) updateBodyParts(requestDto, exercise);

        boolean httpRefsAreDifferent = !exercise.getHttpRefs().stream()
                .map(HttpRef::getId)
                .sorted(Comparator.comparingLong(Long::longValue))
                .toList()
                .equals(requestDto.getHttpRefIds());
        if (httpRefsAreDifferent) updateHttpRefs(requestDto, exercise, user);

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
            BodyPart bodyPart = bodyPartRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
            exercise.getBodyParts().add(bodyPart);
        }

        for (long id : idsToRemove) {
            BodyPart bodyPart = bodyPartRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
            exercise.getBodyParts().remove(bodyPart);
        }
    }

    private void updateHttpRefs(ExerciseUpdateRequestDto requestDto, Exercise exercise, User user) {
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
                    .orElseThrow(() -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));

            if (httpRef.isCustom()) {
                boolean userHasCustomHttpRef =
                        user.getHttpRefs() != null && user.getHttpRefs().contains(httpRef);
                if (!userHasCustomHttpRef)
                    throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);
            }

            exercise.getHttpRefs().add(httpRef);
        }

        for (long id : idsToRemove) {
            HttpRef httpRef = httpRefRepository
                    .findById(id)
                    .orElseThrow(() -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
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

    @Override
    @Transactional
    public void deleteCustomExercise(long exerciseId, long userId) {
        Exercise exercise = exerciseRepository
                .findCustomByExerciseIdAndUserId(exerciseId, userId)
                .orElseThrow(() -> new ApiException(ErrorMessage.NOT_FOUND, HttpStatus.NOT_FOUND));
        userService.deleteUserExercise(userId, exercise);
        exerciseRepository.delete(exercise);
    }
}
