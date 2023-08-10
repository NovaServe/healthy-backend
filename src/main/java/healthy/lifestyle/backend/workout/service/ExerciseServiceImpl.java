package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import healthy.lifestyle.backend.common.ValidationService;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExerciseServiceImpl implements ExerciseService {
    private final ExerciseRepository exerciseRepository;

    private final BodyPartRepository bodyPartRepository;

    private final HttpRefRepository httpRefRepository;

    private final ValidationService validationService;

    public ExerciseServiceImpl(
            ExerciseRepository exerciseRepository,
            BodyPartRepository bodyPartRepository,
            HttpRefRepository httpRefRepository,
            ValidationService validationService) {
        this.exerciseRepository = exerciseRepository;
        this.bodyPartRepository = bodyPartRepository;
        this.httpRefRepository = httpRefRepository;
        this.validationService = validationService;
    }

    @Override
    public CreateExerciseResponseDto createExercise(CreateExerciseRequestDto requestDto, Long userId) {
        validateCreateExerciseRequestDto(requestDto, userId);

        Exercise exercise = new Exercise();
        exercise.setTitle(requestDto.getTitle());
        exercise.setIsCustom(true);

        if (nonNull(requestDto.getDescription())) {
            exercise.setDescription(requestDto.getDescription());
        }

        if (nonNull(requestDto.getBodyParts())) {
            if (isNull(exercise.getBodyParts())) {
                exercise.setBodyParts(new HashSet<BodyPart>());
            }

            for (BodyPartRequestDto elt : requestDto.getBodyParts()) {
                BodyPart bodyPart = bodyPartRepository.getReferenceById(elt.getId());
                exercise.getBodyParts().add(bodyPart);
            }
        }

        if (nonNull(requestDto.getHttpRefs())) {
            if (isNull(exercise.getHttpRefs())) {
                exercise.setHttpRefs(new HashSet<HttpRef>());
            }

            for (HttpRefRequestDto elt : requestDto.getHttpRefs()) {
                HttpRef httpRef = httpRefRepository.getReferenceById(elt.getId());
                exercise.getHttpRefs().add(httpRef);
            }
        }

        Exercise saved = exerciseRepository.save(exercise);
        return mapExerciseToCreateResponseDto(saved);
    }

    private void validateCreateExerciseRequestDto(CreateExerciseRequestDto requestDto, long userId) {
        // Validate fields for not-allowed symbols
        if (!validationService.validatedText(requestDto.getTitle())
                || !validationService.validatedText(requestDto.getDescription())) {
            throw new ApiException(ErrorMessage.INVALID_SYMBOLS, HttpStatus.BAD_REQUEST);
        }

        // Check is there duplicated exercise in the database for this particular user
        if (exerciseTitleDuplicateExists(requestDto.getTitle(), userId)) {
            throw new ApiException(ErrorMessage.TITLE_DUPLICATE, HttpStatus.BAD_REQUEST);
        }

        // Check if body parts are present in the database
        if (nonNull(requestDto.getBodyParts())) {
            if (!bodyPartsExist(requestDto.getBodyParts())) {
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
        }

        // Check if http refs have already been created in the database
        if (nonNull(requestDto.getHttpRefs())) {
            if (!httpRefExists(requestDto.getHttpRefs())) {
                throw new ApiException(ErrorMessage.INVALID_NESTED_OBJECT, HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * Checks if user already has an exercise with the given title.
     * Returns true if there is no duplicates for this particular user, otherwise false.
     * There may be duplicated exercises with same title in the database table,
     * because user can have own exercise with same title as app's pre-defined exercise.
     * However, user can have only one own exercise with certain title.
     *
     * Example: there is, let say, app's pre-defined exercise "Push-up" which user can choose.
     * At the same time, user can add own "Push-up" exercise with another description, refs and body parts.
     * Therefor, there are two exercises in database with same title "Push-up". However, user can have
     * only one own exercise "Push up". Another user can have they own exercise "Push up" in the same database table.
     */
    private boolean exerciseTitleDuplicateExists(String exerciseTitle, long userId) {
        return exerciseRepository.findByTitleAndUserId(exerciseTitle, userId).isPresent();
    }

    /**
     * Returns true if all objects exist, otherwise false.
     */
    private boolean bodyPartsExist(Set<BodyPartRequestDto> bodyParts) {
        for (BodyPartRequestDto elt : bodyParts) {
            if (!bodyPartRepository.existsById(elt.getId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all objects exist, otherwise false.
     */
    private boolean httpRefExists(Set<HttpRefRequestDto> httpRefs) {
        for (HttpRefRequestDto elt : httpRefs) {
            if (!httpRefRepository.existsById(elt.getId())) {
                return false;
            }
        }
        return true;
    }

    private CreateExerciseResponseDto mapExerciseToCreateResponseDto(Exercise exercise) {
        Set<BodyPartResponseDto> bodyPartsDto = null;

        if (nonNull(exercise.getBodyParts())) {
            bodyPartsDto = new HashSet<BodyPartResponseDto>();

            for (BodyPart elt : exercise.getBodyParts()) {
                BodyPartResponseDto bodyPartDto = new BodyPartResponseDto.Builder()
                        .id(elt.getId())
                        .name(elt.getName())
                        .build();
                bodyPartsDto.add(bodyPartDto);
            }
        }

        Set<HttpRefResponseDto> httpRefsDto = null;

        if (nonNull(exercise.getHttpRefs())) {
            httpRefsDto = new HashSet<HttpRefResponseDto>();

            for (HttpRef elt : exercise.getHttpRefs()) {
                HttpRefResponseDto httpRefDto = new HttpRefResponseDto.Builder()
                        .id(elt.getId())
                        .name(elt.getName())
                        .description(elt.getDescription())
                        .ref(elt.getRef())
                        .build();
                httpRefsDto.add(httpRefDto);
            }
        }

        return new CreateExerciseResponseDto.Builder()
                .id(exercise.getId())
                .title(exercise.getTitle())
                .description(exercise.getDescription())
                .bodyParts(bodyPartsDto)
                .httpRefs(httpRefsDto)
                .build();
    }
}
