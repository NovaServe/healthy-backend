package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
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
class WorkoutServiceTest {
    @InjectMocks
    WorkoutServiceImpl workoutService;

    @Mock
    WorkoutRepository workoutRepository;

    @Spy
    ModelMapper modelMapper;

    DataUtil dataUtil = new DataUtil();

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultWorkouts() {
        // Given
        List<Exercise> exercises = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        Workout workout1 = dataUtil.createWorkout(1L, false, Set.of(exercises.get(0), exercises.get(1)));
        Workout workout2 = dataUtil.createWorkout(2L, false, Set.of(exercises.get(2), exercises.get(3)));
        List<Workout> workouts = List.of(workout1, workout2);

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        when(workoutRepository.findAllDefault(sort)).thenReturn(workouts);

        // When
        List<WorkoutResponseDto> responseWorkouts = workoutService.getDefaultWorkouts("id");

        // Then
        verify(workoutRepository, times(1)).findAllDefault(sort);
        assertEquals(2, responseWorkouts.size());

        assertThat(workouts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "bodyParts")
                .isEqualTo(responseWorkouts);
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnDefaultWorkout() {
        // Given
        long workoutId = 3;
        List<Exercise> exercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        List<Workout> workouts = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createWorkout(id, false, Set.of(exercises.get(0), exercises.get(1))))
                .toList();

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workouts.get((int) workoutId)));

        // When
        WorkoutResponseDto responseWorkout = workoutService.getDefaultWorkoutById(workoutId);

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertThat(workouts.get((int) workoutId))
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts")
                .isEqualTo(responseWorkout);
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldThrowNotFound_whenIdNotFound() {
        // Given
        long wrongWorkoutId = 100;
        when(workoutRepository.findById(wrongWorkoutId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getDefaultWorkoutById(wrongWorkoutId);
        });

        // Then
        verify(workoutRepository, times(1)).findById(wrongWorkoutId);
        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldThrowUnauthorizedForThisResource_whenWorkoutIsCustom() {
        // Given
        long workoutId = 1;
        List<Exercise> exercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createExercise(id, true, false, false, 1, 2, 1, 2))
                .toList();
        Workout customWorkout = dataUtil.createWorkout(workoutId, true, Set.of(exercises.get(0), exercises.get(1)));

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(customWorkout));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getDefaultWorkoutById(workoutId);
        });

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertEquals(ErrorMessage.UNAUTHORIZED_FOR_THIS_RESOURCE.getName(), exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }
}
