package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {
    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private BodyPartRepository bodyPartRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    ExerciseServiceImpl exerciseService;

    DataUtil dataUtil = new DataUtil();

    @Test
    void createExerciseTest_shouldReturnNewExercise() {
        // Given
        CreateExerciseRequestDto requestDto = dataUtil.createExerciseRequestDto(1, false, 1, 2, 1, 2);
        List<BodyPart> bodyPartsMock = dataUtil.createBodyParts(1, 2);
        List<HttpRef> httpRefsMock = dataUtil.createHttpRefs(1, 2, false);
        Exercise exerciseMock = dataUtil.createExercise(1, false, false, false, 1, 2, 1, 2);
        long userId = 1L;
        when(bodyPartRepository.existsById(anyLong())).thenReturn(true);
        when(bodyPartRepository.getReferenceById(1L)).thenReturn(bodyPartsMock.get(0));
        when(bodyPartRepository.getReferenceById(2L)).thenReturn(bodyPartsMock.get(1));
        when(httpRefRepository.existsById(anyLong())).thenReturn(true);
        when(httpRefRepository.getReferenceById(1L)).thenReturn(httpRefsMock.get(0));
        when(httpRefRepository.getReferenceById(2L)).thenReturn(httpRefsMock.get(1));
        when(exerciseRepository.findCustomByTitleAndUserId(requestDto.getTitle(), userId))
                .thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exerciseMock);

        // When
        ExerciseResponseDto exerciseActual = exerciseService.createExercise(requestDto, userId);

        // Then
        assertEquals(exerciseMock.getId(), exerciseActual.getId());

        assertThat(exerciseMock)
                .usingRecursiveComparison()
                .ignoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exerciseActual);

        assertThat(bodyPartsMock)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseActual.getBodyParts());

        assertThat(httpRefsMock)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseActual.getHttpRefs());
    }

    @Test
    void getExercisesTest_shouldReturnDefaultExercises() {
        // Given
        List<Exercise> exercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, true, 1, 2, 1, 2))
                .toList();
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        when(exerciseRepository.findAllDefault(sort)).thenReturn(exercises);

        // When
        List<ExerciseResponseDto> exercisesDtoActual = exerciseService.getDefaultExercises();

        // Then
        verify(exerciseRepository, times(1)).findAllDefault(sort);

        org.hamcrest.MatcherAssert.assertThat(exercisesDtoActual, hasSize(exercises.size()));

        assertThat(exercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exercisesDtoActual);

        IntStream.range(0, exercises.size()).forEach(id -> {
            List<BodyPart> bodyParts_ = exercises.get(id).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();

            assertThat(bodyParts_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(exercisesDtoActual.get(id).getBodyParts());

            List<HttpRef> httpRefs_ = exercises.get(id).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();

            assertThat(httpRefs_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(exercisesDtoActual.get(id).getHttpRefs());
        });
    }

    @Test
    void getExercisesTest_shouldReturnCustomExercises() {
        // Given
        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        List<Exercise> customExercises = IntStream.rangeClosed(3, 4)
                .mapToObj(id -> dataUtil.createExercise(id, true, false, true, 3, 4, 3, 4))
                .toList();

        long userId = 1L;
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        when(exerciseRepository.findCustomByUserId(userId, sort)).thenReturn(customExercises);

        // When
        List<ExerciseResponseDto> exercisesDtoActual = exerciseService.getCustomExercises(userId);

        // Then
        verify((exerciseRepository), times(1)).findCustomByUserId(userId, sort);

        org.hamcrest.MatcherAssert.assertThat(exercisesDtoActual, hasSize(customExercises.size()));

        assertThat(customExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exercisesDtoActual);

        IntStream.range(0, customExercises.size()).forEach(id -> {
            List<BodyPart> bodyParts_ = customExercises.get(id).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();

            assertThat(bodyParts_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(exercisesDtoActual.get(id).getBodyParts());

            List<HttpRef> httpRefs_ = customExercises.get(id).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();

            assertThat(httpRefs_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(exercisesDtoActual.get(id).getHttpRefs());
        });
    }
}
