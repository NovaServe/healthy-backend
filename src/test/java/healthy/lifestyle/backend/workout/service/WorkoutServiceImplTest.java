package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class WorkoutServiceImplTest {
    @InjectMocks
    WorkoutServiceImpl workoutService;

    @Mock
    WorkoutRepository workoutRepository;

    @Spy
    ModelMapper modelMapper;

    DataUtil dataUtil = new DataUtil();

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultExercises() {
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
    void getWorkoutByIdTest_shouldReturnWorkout() {
        // Given
        int workoutId = 3;
        List<Exercise> exercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        List<Workout> workouts = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createWorkout(1L, false, Set.of(exercises.get(0), exercises.get(1))))
                .toList();

        when(workoutRepository.findById(workoutId)).thenReturn(workouts.get(workoutId));

        // When
        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(workoutId);

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertThat(workouts.get(workoutId))
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts")
                .isEqualTo(responseWorkout);
    }
}
