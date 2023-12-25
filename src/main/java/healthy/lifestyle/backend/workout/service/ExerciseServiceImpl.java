package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;

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
        exerciseRepository
                .findCustomByTitleAndUserId(requestDto.getTitle(), userId)
                .ifPresent(alreadyExistentWithSameTitle -> {
                    throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);
                });

        Exercise exercise = Exercise.builder()
                .title(requestDto.getTitle())
                .isCustom(true)
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
                                () -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));
                exercise.getBodyParts().add(bodyPart);
            });

        if (requestDto.getHttpRefs() != null && requestDto.getHttpRefs().size() > 0)
            requestDto.getHttpRefs().forEach(id -> {
                HttpRef httpRef = httpRefRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST));

                if (httpRef.getUser().getId() != userId)
                    throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

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
                .orElseThrow(() -> new ApiException(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

        if ((exercise.isCustom() && requiredDefault) || (!exercise.isCustom() && !requiredDefault))
            throw new ApiException(ErrorMessage.DEFAULT_CUSTOM_MISMATCH, HttpStatus.BAD_REQUEST);

        if (userId != null) {
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
        return exerciseRepository.findCustomByUserId(userId, sort).stream()
                .map(elt -> modelMapper.map(elt, ExerciseResponseDto.class))
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
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return exerciseRepository.findAllDefault(sort).stream()
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
                .orElseThrow(() -> new ApiException(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));

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
        if (user == null) throw new ApiException(ErrorMessage.RELATED_RESOURCE_NOT_FOUND, HttpStatus.BAD_REQUEST);

        boolean userHasCustomExercise =
                user.getExercises() != null && user.getExercises().contains(exercise);
        if (!userHasCustomExercise) throw new ApiException(ErrorMessage.USER_RESOURCE_MISMATCH, HttpStatus.BAD_REQUEST);

        if (requestDto.getTitle() != null) {
            boolean titlesAreDifferent = !requestDto.getTitle().equals(exercise.getTitle());
            if (titlesAreDifferent) {
                exerciseRepository
                        .findCustomByTitleAndUserId(requestDto.getTitle(), userId)
                        .ifPresent(existingExercise -> {
                            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);
                        });

                exercise.setTitle(requestDto.getTitle());
            } else throw new ApiException(ErrorMessage.TITLE_IS_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getDescription() != null) {
            boolean descriptionsAreDifferent = !requestDto.getDescription().equals(exercise.getDescription());
            if (descriptionsAreDifferent) exercise.setDescription(requestDto.getDescription());
            else throw new ApiException(ErrorMessage.DESCRIPTION_IS_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
        }

        if (requestDto.getNeedsEquipment() != null) {
            boolean needsEquipmentAreDifferent = (requestDto.getNeedsEquipment() != exercise.isNeedsEquipment());
            if (needsEquipmentAreDifferent) exercise.setNeedsEquipment(requestDto.getNeedsEquipment());
            else throw new ApiException(ErrorMessage.NEEDS_EQUIPMENT_IS_NOT_DIFFERENT, HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new ApiException(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
        userService.deleteUserExercise(userId, exercise);
        exerciseRepository.delete(exercise);
    }
}
