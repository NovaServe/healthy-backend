package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.workout.dto.CreateWorkoutRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
import java.util.Collections;
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

    @Mock
    UserServiceImpl userService;

    @Mock
    ExerciseRepository exerciseRepository;

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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "bodyParts", "users")
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
                .ignoringFields("exercises", "bodyParts", "users")
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

    @Test
    void createCustomWorkoutTest_shouldCreateNewWorkout() {
        // Given
        User user = dataUtil.createUserEntity(1);
        // 2 user custom exercises
        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        exercise1.setUsers(Set.of(user));
        Exercise exercise2 = dataUtil.createExercise(2, true, false, false, 3, 4, 3, 4);
        exercise2.setUsers(Set.of(user));

        // 2 default exercises
        Exercise exercise3 = dataUtil.createExercise(3, false, true, true, 5, 6, 5, 6);
        Exercise exercise4 = dataUtil.createExercise(4, false, false, false, 7, 8, 7, 8);

        CreateWorkoutRequestDto requestDto = dataUtil.createWorkoutRequestDto(
                1, List.of(exercise1.getId(), exercise2.getId(), exercise3.getId(), exercise4.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise1));
        when(exerciseRepository.findById(2L)).thenReturn(Optional.of(exercise2));
        when(exerciseRepository.findById(3L)).thenReturn(Optional.of(exercise3));
        when(exerciseRepository.findById(4L)).thenReturn(Optional.of(exercise4));
        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> {
            Workout saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        doNothing().when(userService).addWorkout(any(User.class), any(Workout.class));

        // When
        WorkoutResponseDto workoutResponseDto = workoutService.createCustomWorkout(user.getId(), requestDto);

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(exerciseRepository, times(4)).findById(anyLong());
        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(userService, times(1)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(1L, workoutResponseDto.getId());
        assertEquals(requestDto.getTitle(), workoutResponseDto.getTitle());
        assertEquals(requestDto.getDescription(), workoutResponseDto.getDescription());
        assertTrue(workoutResponseDto.isCustom());
        assertTrue(workoutResponseDto.isNeedsEquipment());
        assertEquals(8, workoutResponseDto.getBodyParts().size());
        assertEquals(4, workoutResponseDto.getExercises().size());
    }

    @Test
    void createCustomWorkoutTest_shouldReturnTitleDuplicateAnd400_whenWorkoutWithSameTitleExists() {
        // Given
        User user = dataUtil.createUserEntity(1);
        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        exercise1.setUsers(Set.of(user));

        Workout workout = dataUtil.createWorkout(1L, true, null);

        CreateWorkoutRequestDto requestDto = dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(List.of(workout));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.createCustomWorkout(user.getId(), requestDto);
        });

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(userService, times(0)).getUserById(user.getId());
        verify(exerciseRepository, times(0)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(userService, times(0)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(ErrorMessage.TITLE_DUPLICATE.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void createCustomWorkoutTest_shouldReturnInvalidNestedObjectAnd400_whenExerciseNotFound() {
        // Given
        User user = dataUtil.createUserEntity(1);
        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        exercise1.setUsers(Set.of(user));

        CreateWorkoutRequestDto requestDto = dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findById(exercise1.getId())).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.createCustomWorkout(user.getId(), requestDto);
        });

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(exerciseRepository, times(1)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(userService, times(0)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(ErrorMessage.INVALID_NESTED_OBJECT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void createCustomWorkoutTest_shouldReturnUserResourceMismatchAnd400_whenExerciseBelongsToAnotherUser() {
        // Given
        User user = dataUtil.createUserEntity(1);
        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        exercise1.setUsers(Set.of(user));

        User user2 = dataUtil.createUserEntity(2);
        Exercise exercise2 = dataUtil.createExercise(2, true, true, true, 3, 4, 3, 4);
        exercise2.setUsers(Set.of(user2));

        CreateWorkoutRequestDto requestDto =
                dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId(), exercise2.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findById(exercise1.getId())).thenReturn(Optional.of(exercise1));
        when(exerciseRepository.findById(exercise2.getId())).thenReturn(Optional.of(exercise2));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.createCustomWorkout(user.getId(), requestDto);
        });

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(exerciseRepository, times(2)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(userService, times(0)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }
}
