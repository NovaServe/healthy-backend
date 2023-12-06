package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.*;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {
    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private BodyPartRepository bodyPartRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Mock
    private UserServiceImpl userService;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    ExerciseServiceImpl exerciseService;

    DataUtil dataUtil = new DataUtil();

    @Test
    void createExerciseTest_shouldReturnNewExercise() {
        // Given
        ExerciseCreateRequestDto requestDto =
                dataUtil.createExerciseRequestDto(1, false, new Long[] {1L, 2L}, new Long[] {1L, 2L});

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

        doNothing().when(userService).addExercise(any(Long.class), any(Exercise.class));

        // When
        ExerciseResponseDto exerciseActual = exerciseService.createExercise(requestDto, userId);

        // Then
        verify(bodyPartRepository, times(2)).existsById(anyLong());
        verify(bodyPartRepository, times(2)).getReferenceById(anyLong());
        verify(httpRefRepository, times(2)).existsById(anyLong());
        verify(httpRefRepository, times(2)).getReferenceById(anyLong());
        verify(exerciseRepository, times(1)).findCustomByTitleAndUserId(eq(requestDto.getTitle()), eq(userId));
        verify(exerciseRepository, times(1)).save(any(Exercise.class));
        verify(userService, times(1)).addExercise(eq(userId), any());

        assertEquals(exerciseMock.getId(), exerciseActual.getId());

        assertThat(exerciseMock)
                .usingRecursiveComparison()
                .ignoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exerciseActual);

        assertThat(bodyPartsMock)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseActual.getBodyParts());

        assertThat(httpRefsMock)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exerciseActual.getHttpRefs());
    }

    @Test
    void getDefaultExercisesTest_shouldReturnDefaultExercises() {
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
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(exercisesDtoActual.get(id).getHttpRefs());
        });
    }

    @Test
    void getCustomExercisesTest_shouldReturnCustomExercises() {
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
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(exercisesDtoActual.get(id).getHttpRefs());
        });
    }

    @Test
    void getExerciseByIdTest_shouldReturnDefaultExercise_whenDefaultExerciseRequested() {
        // Given
        Exercise exercise = dataUtil.createExercise(1L, false, false, false, 1, 2, 1, 2);
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

        // When
        ExerciseResponseDto exerciseDtoActual = exerciseService.getExerciseById(exercise.getId(), true, null);

        // Then
        verify((exerciseRepository), times(1)).findById(exercise.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertThat(exercise)
                .usingRecursiveComparison()
                .ignoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exerciseDtoActual);

        List<BodyPart> bodyParts_ = exercise.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(bodyParts_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseDtoActual.getBodyParts());

        List<HttpRef> httpRefs_ = exercise.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(httpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exerciseDtoActual.getHttpRefs());
    }

    @Test
    void getExerciseByIdTest_shouldReturnCustomExercise_whenCustomExerciseRequested() {
        // Given
        Exercise exercise = dataUtil.createExercise(1L, true, false, false, 1, 2, 1, 2);
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

        User user = dataUtil.createUserEntity(1L);
        user.setExercises(new HashSet<>() {
            {
                add(exercise);
            }
        });
        when(userService.getUserById(user.getId())).thenReturn(user);

        // When
        ExerciseResponseDto exerciseDtoActual = exerciseService.getExerciseById(exercise.getId(), false, user.getId());

        // Then
        verify((exerciseRepository), times(1)).findById(exercise.getId());
        verify(userService, times(1)).getUserById(user.getId());

        Assertions.assertThat(exercise)
                .usingRecursiveComparison()
                .ignoringFields("users", "bodyParts", "httpRefs")
                .isEqualTo(exerciseDtoActual);

        List<BodyPart> bodyParts_ = exercise.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(bodyParts_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseDtoActual.getBodyParts());

        List<HttpRef> httpRefs_ = exercise.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(httpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exerciseDtoActual.getHttpRefs());
    }

    @Test
    void getExerciseByIdTest_shouldThrowNotFoundAnd404_whenExerciseNotFound() {
        // Given
        long exerciseId = 1L;
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> exerciseService.getExerciseById(exerciseId, true, null));

        // Then
        verify((exerciseRepository), times(1)).findById(exerciseId);
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
    }

    @Test
    void getExerciseByIdTest_shouldThrowDefaultCustomMismatchAnd400_whenCustomExerciseAndDefaultRequestOccurred() {
        // Given
        Exercise exercise = dataUtil.createExercise(1L, true, false, true, 1, 2, 1, 2);
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> exerciseService.getExerciseById(exercise.getId(), true, null));

        // Then
        verify((exerciseRepository), times(1)).findById(exercise.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(ErrorMessage.DEFAULT_CUSTOM_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void getExerciseByIdTest_shouldThrowDefaultCustomMismatchAnd400_whenDefaultExerciseAndCustomRequestOccurred() {
        // Given
        Exercise exercise = dataUtil.createExercise(1L, false, false, false, 1, 2, 1, 2);
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> exerciseService.getExerciseById(exercise.getId(), false, null));

        // Then
        verify((exerciseRepository), times(1)).findById(exercise.getId());
        verify(userService, times(0)).getUserById(anyLong());

        assertEquals(ErrorMessage.DEFAULT_CUSTOM_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void getExerciseByIdTest_shouldThrowUserResourceMismatchAnd400_whenRequestedExerciseDoesntBelongToUser() {
        // Given
        Exercise exercise = dataUtil.createExercise(1L, true, false, false, 1, 2, 1, 2);
        User user = dataUtil.createUserEntity(1L);
        user.setExercises(new HashSet<>() {
            {
                add(exercise);
            }
        });
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

        Exercise exercise2 = dataUtil.createExercise(2L, true, false, false, 3, 4, 3, 4);
        User user2 = dataUtil.createUserEntity(2L);
        user2.setExercises(new HashSet<>() {
            {
                add(exercise2);
            }
        });
        when(userService.getUserById(user2.getId())).thenReturn(user2);

        // When
        ApiException exception = assertThrows(
                ApiException.class, () -> exerciseService.getExerciseById(exercise.getId(), false, user2.getId()));

        // Then
        verify((exerciseRepository), times(1)).findById(exercise.getId());
        verify(userService, times(1)).getUserById(user2.getId());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }
}
