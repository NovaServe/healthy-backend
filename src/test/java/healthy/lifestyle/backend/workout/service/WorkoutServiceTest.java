package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.workout.dto.WorkoutCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.dto.WorkoutUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.WorkoutRepository;
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
        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(workoutId, false);

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
            workoutService.getWorkoutById(wrongWorkoutId, false);
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
            workoutService.getWorkoutById(workoutId, false);
        });

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertEquals(ErrorMessage.UNAUTHORIZED_FOR_THIS_RESOURCE.getName(), exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void getCustomWorkoutByIdTest_shouldReturnCustomWorkout() {
        // Given
        long workoutId = 2;

        List<Exercise> customExercises = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createExercise(id, true, false, false, 1, 2, 1, 2))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        Workout workout1 = dataUtil.createWorkout(0L, true, Set.of(customExercises.get(0), customExercises.get(1)));
        Workout workout2 = dataUtil.createWorkout(1L, false, Set.of(defaultExercises.get(0), defaultExercises.get(1)));
        Workout workout3 = dataUtil.createWorkout(2L, true, Set.of(customExercises.get(2), customExercises.get(3)));
        Workout workout4 = dataUtil.createWorkout(3L, false, Set.of(defaultExercises.get(2), defaultExercises.get(3)));
        List<Workout> workouts = List.of(workout1, workout2, workout3, workout4);

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workouts.get((int) workoutId)));

        // When
        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(workoutId, true);

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertThat(workouts.get((int) workoutId))
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "users")
                .isEqualTo(responseWorkout);
    }

    @Test
    void getCustomWorkoutByIdTest_shouldThrowCustomWorkoutRequired_whenWorkoutIsDefault() {
        // Given
        long workoutId = 1;

        List<Exercise> customExercises = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createExercise(id, true, false, false, 1, 2, 1, 2))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 4)
                .mapToObj(id -> dataUtil.createExercise(id, false, false, false, 1, 2, 1, 2))
                .toList();

        Workout workout1 = dataUtil.createWorkout(0L, true, Set.of(customExercises.get(0), customExercises.get(1)));
        Workout workout2 = dataUtil.createWorkout(1L, false, Set.of(defaultExercises.get(0), defaultExercises.get(1)));
        Workout workout3 = dataUtil.createWorkout(2L, true, Set.of(customExercises.get(2), customExercises.get(3)));
        Workout workout4 = dataUtil.createWorkout(3L, false, Set.of(defaultExercises.get(2), defaultExercises.get(3)));
        List<Workout> workouts = List.of(workout1, workout2, workout3, workout4);

        when(workoutRepository.findById(workoutId)).thenReturn(Optional.of(workouts.get((int) workoutId)));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getWorkoutById(workoutId, true);
        });

        // Then
        verify(workoutRepository, times(1)).findById(workoutId);
        assertEquals(ErrorMessage.CUSTOM_WORKOUT_REQUIRED.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
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

        WorkoutCreateRequestDto requestDto = dataUtil.createWorkoutRequestDto(
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

        WorkoutCreateRequestDto requestDto = dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId()));

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

        WorkoutCreateRequestDto requestDto = dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId()));

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

        WorkoutCreateRequestDto requestDto =
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

    @Test
    void updateCustomWorkoutTest_shouldReturnWorkoutResponseDto_whenValidRequestDtoProvided() {
        User user = dataUtil.createUserEntity(1);

        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        Exercise exercise2 = dataUtil.createExercise(2, true, true, true, 3, 4, 3, 4);
        Exercise exercise3 = dataUtil.createExercise(3, true, true, false, 5, 6, 5, 6);
        Exercise exercise4 = dataUtil.createExercise(4, false, true, false, 8, 9, 8, 9);

        user.setExercises(new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
                add(exercise3);
                add(exercise4);
            }
        });

        Workout workout = dataUtil.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
            }
        });
        user.setWorkouts(new HashSet<>() {
            {
                add(workout);
            }
        });

        Set<BodyPart> expectedBodyPartsSet = new HashSet<>() {
            {
                addAll(exercise2.getBodyParts());
                addAll(exercise3.getBodyParts());
                addAll(exercise4.getBodyParts());
            }
        };
        List<BodyPart> expectedBodyPartList = expectedBodyPartsSet.stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();

        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, new ArrayList<>() {
            {
                add(exercise2.getId());
                add(exercise3.getId());
                add(exercise4.getId());
            }
        });

        when(userService.getUserById(1)).thenReturn(user);
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        when(exerciseRepository.findById(exercise1.getId())).thenReturn(Optional.of(exercise1));
        when(exerciseRepository.findById(exercise3.getId())).thenReturn(Optional.of(exercise3));
        when(exerciseRepository.findById(exercise4.getId())).thenReturn(Optional.of(exercise4));

        // When
        WorkoutResponseDto responseDto = workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto);

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(workout.getId());
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(exercise1.getId());
        verify(exerciseRepository, times(1)).findById(exercise3.getId());
        verify(exerciseRepository, times(1)).findById(exercise4.getId());

        assertEquals(workout.getId(), responseDto.getId());
        assertEquals(requestDto.getTitle(), responseDto.getTitle());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertTrue(responseDto.isCustom());
        assertTrue(responseDto.isNeedsEquipment());

        assertThat(responseDto.getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(expectedBodyPartList);

        assertThat(responseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(List.of(exercise2, exercise3, exercise4));

        assertThat(responseDto.getExercises().get(0).getBodyParts())
                .usingRecursiveComparison()
                .ignoringFields("exercises")
                .isEqualTo(exercise2.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getBodyParts())
                .usingRecursiveComparison()
                .ignoringFields("exercises")
                .isEqualTo(exercise3.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(2).getBodyParts())
                .usingRecursiveComparison()
                .ignoringFields("exercises")
                .isEqualTo(exercise4.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(0).getHttpRefs())
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(exercise2.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getHttpRefs())
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(exercise3.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(2).getHttpRefs())
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(exercise4.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowEmptyRequestExceptionAnd400_whenEmptyDtoProvided() {
        // Given
        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());
        requestDto.setTitle(null);
        requestDto.setDescription(null);

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(1L, 2L, requestDto);
        });

        // Then
        verify(userService, times(0)).getUserById(1);
        verify(workoutRepository, times(0)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.EMPTY_REQUEST.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowNotFoundExceptionAnd400_whenWorkoutNotFound() {
        // Given
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(user.getId())).thenReturn(user);
        long workoutId = 1L;
        when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());
        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workoutId, requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(workoutId);
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenUserDoesntHaveWorkouts() {
        // Given
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(user.getId())).thenReturn(user);
        Workout workout = dataUtil.createWorkout(1L, true, null);
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(workout.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenWorkoutDoesntBelongToUser() {
        // Given
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(user.getId())).thenReturn(user);
        Workout workout = dataUtil.createWorkout(1L, true, null);
        user.setWorkouts(new HashSet<>() {
            {
                add(workout);
            }
        });
        Workout workout2 = dataUtil.createWorkout(2L, true, null);
        when(workoutRepository.findById(workout2.getId())).thenReturn(Optional.of(workout2));
        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workout2.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(workout2.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowTitleDuplicateAnd400_whenWorkoutTitleDuplicated() {
        // Given
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(user.getId())).thenReturn(user);
        Workout workout = dataUtil.createWorkout(1L, true, null);
        user.setWorkouts(new HashSet<>() {
            {
                add(workout);
            }
        });
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());
        Workout workout2 = dataUtil.createWorkout(2L, true, null);
        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(List.of(workout2));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(workout.getId());
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.TITLE_DUPLICATE.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowInvalidNestedObjectAnd400_whenExerciseNotFound() {
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(1)).thenReturn(user);

        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        Exercise exercise2 = dataUtil.createExercise(2, true, true, true, 3, 4, 3, 4);
        user.setExercises(new HashSet<>());
        user.getExercises().add(exercise1);
        user.getExercises().add(exercise2);
        Workout workout = dataUtil.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
            }
        });
        user.setWorkouts(new HashSet<>());
        user.getWorkouts().add(workout);
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

        Exercise exercise3 = dataUtil.createExercise(3, true, true, false, 5, 6, 5, 6);
        user.getExercises().add(exercise3);

        // Default exercise
        Exercise exercise4 = dataUtil.createExercise(4, false, true, false, 8, 9, 8, 9);

        long nonExistingExerciseId = 999L;

        when(exerciseRepository.findById(exercise3.getId())).thenReturn(Optional.of(exercise3));
        when(exerciseRepository.findById(exercise4.getId())).thenReturn(Optional.of(exercise4));
        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());

        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, new ArrayList<>() {
            {
                add(exercise1.getId());
                add(exercise2.getId());
                add(exercise3.getId());
                add(exercise4.getId());
                add(nonExistingExerciseId);
            }
        });

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(workout.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(exercise3.getId());
        verify(exerciseRepository, times(1)).findById(exercise4.getId());
        verify(exerciseRepository, times(1)).findById(nonExistingExerciseId);

        assertEquals(ErrorMessage.INVALID_NESTED_OBJECT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenExerciseDoesntBelongToUser() {
        User user = dataUtil.createUserEntity(1);
        when(userService.getUserById(1)).thenReturn(user);

        Exercise exercise1 = dataUtil.createExercise(1, true, true, true, 1, 2, 1, 2);
        Exercise exercise2 = dataUtil.createExercise(2, true, true, true, 3, 4, 3, 4);
        user.setExercises(new HashSet<>());
        user.getExercises().add(exercise1);
        user.getExercises().add(exercise2);
        Workout workout = dataUtil.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
            }
        });
        user.setWorkouts(new HashSet<>());
        user.getWorkouts().add(workout);
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

        Exercise exercise3 = dataUtil.createExercise(3, true, true, false, 5, 6, 5, 6);
        user.getExercises().add(exercise3);

        // Default exercise
        Exercise exercise4 = dataUtil.createExercise(4, false, true, false, 8, 9, 8, 9);

        // Another user
        Exercise exercise5 = dataUtil.createExercise(5, true, true, false, 10, 11, 10, 11);
        User user2 = dataUtil.createUserEntity(2);
        user2.setExercises(new HashSet<>());
        user2.getExercises().add(exercise5);

        when(exerciseRepository.findById(exercise3.getId())).thenReturn(Optional.of(exercise3));
        when(exerciseRepository.findById(exercise4.getId())).thenReturn(Optional.of(exercise4));
        when(exerciseRepository.findById(exercise5.getId())).thenReturn(Optional.of(exercise5));

        WorkoutUpdateRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, new ArrayList<>() {
            {
                add(exercise1.getId());
                add(exercise2.getId());
                add(exercise3.getId());
                add(exercise4.getId());
                add(exercise5.getId());
            }
        });

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(workout.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(exercise3.getId());
        verify(exerciseRepository, times(1)).findById(exercise4.getId());
        verify(exerciseRepository, times(1)).findById(exercise5.getId());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void deleteCustomWorkoutTest_shouldReturnDeletedId_whenValidUserIdAndWorkoutIdProvided() {
        // Given
        User user = dataUtil.createUserEntity(1);
        Workout workout = dataUtil.createWorkout(1, true, null);
        user.setWorkouts(new HashSet<>() {
            {
                add(workout);
            }
        });
        when(workoutRepository.findCustomByWorkoutIdAndUserId(workout.getId(), user.getId()))
                .thenReturn(List.of(workout));

        // When
        long id = workoutService.deleteCustomWorkout(user.getId(), workout.getId());

        // Then
        verify(workoutRepository, times(1)).findCustomByWorkoutIdAndUserId(workout.getId(), user.getId());
        assertEquals(workout.getId(), id);
    }

    @Test
    void deleteCustomWorkoutTest_shouldThrowNotFoundAnd400_whenWorkoutNotFound() {
        // Given
        long userId = 1;
        long workoutId = 2;
        when(workoutRepository.findCustomByWorkoutIdAndUserId(workoutId, userId))
                .thenReturn(Collections.emptyList());

        // When
        ApiException exception =
                assertThrows(ApiException.class, () -> workoutService.deleteCustomWorkout(userId, workoutId));

        // Then
        verify(workoutRepository, times(1)).findCustomByWorkoutIdAndUserId(workoutId, userId);
        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }
}
