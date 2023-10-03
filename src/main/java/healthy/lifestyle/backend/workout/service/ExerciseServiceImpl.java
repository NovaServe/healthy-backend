package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.Exercise;
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

    private final ModelMapper modelMapper;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            HttpRefRepository httpRefRepository,
            ModelMapper modelMapper) {
        this.exerciseRepository = exerciseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.httpRefRepository = httpRefRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, long userId) {
        validateCreateExerciseRequestDto(requestDto, userId);

        Exercise exercise = Exercise.builder()
                .title(requestDto.getTitle())
                .isCustom(true)
                .bodyParts(new HashSet<>())
                .httpRefs(new HashSet<>())
                .build();

        if (nonNull(requestDto.getDescription())) exercise.setDescription(requestDto.getDescription());

        if (nonNull(requestDto.getBodyParts())) {
            requestDto.getBodyParts().forEach(elt -> exercise.getBodyParts()
                    .add(bodyPartRepository.getReferenceById(elt.getId())));
        }

        if (nonNull(requestDto.getHttpRefs())) {
            requestDto.getHttpRefs().forEach(elt -> exercise.getHttpRefs()
                    .add(httpRefRepository.getReferenceById(elt.getId())));
        }

        Exercise saved = exerciseRepository.save(exercise);

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

        // Check if body parts are present in the database
        if (nonNull(requestDto.getBodyParts()) && requestDto.getBodyParts().size() > 0) {
            if (!bodyPartsExist(requestDto.getBodyParts()))
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
        } else throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);

        // Check if http refs have already been created in the database
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
    private boolean bodyPartsExist(List<BodyPartRequestDto> bodyParts) {
        for (BodyPartRequestDto elt : bodyParts) {
            if (!bodyPartRepository.existsById(elt.getId())) return false;
        }
        return true;
    }

    /**
     * Returns true if all objects exist, otherwise false.
     */
    private boolean httpRefExists(List<HttpRefRequestDto> httpRefs) {
        for (HttpRefRequestDto elt : httpRefs) {
            if (!httpRefRepository.existsById(elt.getId())) return false;
        }
        return true;
    }

    @Transactional
    @Override
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
}
