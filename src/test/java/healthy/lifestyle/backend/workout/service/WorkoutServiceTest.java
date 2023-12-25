package healthy.lifestyle.backend.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
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

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();

    @Test
    void createCustomWorkoutTest_shouldCreateNewWorkout() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);

        WorkoutCreateRequestDto requestDto =
                dtoUtil.workoutCreateRequestDto(1, List.of(customExercise.getId(), defaultExercise.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
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
        verify(exerciseRepository, times(2)).findById(anyLong());
        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(userService, times(1)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(1L, workoutResponseDto.getId());
        assertEquals(requestDto.getTitle(), workoutResponseDto.getTitle());
        assertEquals(requestDto.getDescription(), workoutResponseDto.getDescription());
        assertTrue(workoutResponseDto.isCustom());
        assertTrue(workoutResponseDto.isNeedsEquipment());
        assertEquals(2, workoutResponseDto.getExercises().size());
        assertEquals(2, workoutResponseDto.getBodyParts().size());
    }

    @Test
    void createCustomWorkoutTest_shouldReturnTitleDuplicateAnd400_whenWorkoutWithSameTitleExists() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout alreadyExistedWorkout = testUtil.createCustomWorkout(1, List.of(customExercise), user);

        WorkoutCreateRequestDto requestDto =
                dtoUtil.workoutCreateRequestDto(1, List.of(customExercise.getId(), defaultExercise.getId()));
        requestDto.setTitle(alreadyExistedWorkout.getTitle());

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(List.of(alreadyExistedWorkout));

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
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        long nonExistingExerciseId = customExercise.getId() + 1;

        WorkoutCreateRequestDto requestDto = dtoUtil.workoutCreateRequestDto(
                1, List.of(defaultExercise.getId(), customExercise.getId(), nonExistingExerciseId));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.createCustomWorkout(user.getId(), requestDto);
        });

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(exerciseRepository, times(3)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(userService, times(0)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(ErrorMessage.INVALID_NESTED_OBJECT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void createCustomWorkoutTest_shouldReturnUserResourceMismatchAnd400_whenExerciseBelongsToAnotherUser() {
        // Given
        User user1 = testUtil.createUser(1);
        User user2 = testUtil.createUser(2);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = testUtil.createCustomHttpRef(2, user1);
        HttpRef customHttpRef2 = testUtil.createCustomHttpRef(3, user2);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise1 =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user1);
        Exercise customExercise2 =
                testUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart2), List.of(customHttpRef2), user2);

        WorkoutCreateRequestDto requestDto = dtoUtil.workoutCreateRequestDto(
                1, List.of(defaultExercise.getId(), customExercise1.getId(), customExercise2.getId()));

        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user1.getId()))
                .thenReturn(Collections.emptyList());
        when(userService.getUserById(user1.getId())).thenReturn(user1);
        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
        when(exerciseRepository.findById(customExercise1.getId())).thenReturn(Optional.of(customExercise1));
        when(exerciseRepository.findById(customExercise2.getId())).thenReturn(Optional.of(customExercise2));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.createCustomWorkout(user1.getId(), requestDto);
        });

        // Then
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user1.getId());
        verify(userService, times(1)).getUserById(user1.getId());
        verify(exerciseRepository, times(3)).findById(anyLong());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(userService, times(0)).addWorkout(any(User.class), any(Workout.class));

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnDefaultWorkout() {
        // Given
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Workout defaultWorkout1 = testUtil.createDefaultWorkout(1, List.of(defaultExercise1));

        when(workoutRepository.findById(defaultWorkout1.getId())).thenReturn(Optional.of(defaultWorkout1));

        // When
        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(defaultWorkout1.getId(), false);

        // Then
        verify(workoutRepository, times(1)).findById(defaultWorkout1.getId());
        assertThat(defaultWorkout1)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "user")
                .isEqualTo(responseWorkout);
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldThrowNotFound_whenIdNotFound() {
        // Given
        long wrongWorkoutId = 1000L;
        when(workoutRepository.findById(wrongWorkoutId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getWorkoutById(wrongWorkoutId, false);
        });

        // Then
        verify(workoutRepository, times(1)).findById(wrongWorkoutId);
        assertEquals(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldThrowUnauthorizedForThisResource_whenWorkoutIsCustom() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Workout customWorkout1 = testUtil.createCustomWorkout(1, List.of(defaultExercise1), user);

        when(workoutRepository.findById(customWorkout1.getId())).thenReturn(Optional.of(customWorkout1));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getWorkoutById(customWorkout1.getId(), false);
        });

        // Then
        verify(workoutRepository, times(1)).findById(customWorkout1.getId());
        assertEquals(ErrorMessage.UNAUTHORIZED_FOR_THIS_RESOURCE.getName(), exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultWorkouts() {
        // Given
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = testUtil.createDefaultHttpRef(2);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                testUtil.createDefaultExercise(2, needsEquipment, List.of(bodyPart2), List.of(defaultHttpRef2));

        Workout defaultWorkout1 = testUtil.createDefaultWorkout(1, List.of(defaultExercise1));
        Workout defaultWorkout2 = testUtil.createDefaultWorkout(2, List.of(defaultExercise2));
        List<Workout> defaultWorkouts = List.of(defaultWorkout1, defaultWorkout2);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        when(workoutRepository.findAllDefault(sort)).thenReturn(defaultWorkouts);

        // When
        List<WorkoutResponseDto> responseWorkouts = workoutService.getDefaultWorkouts("id");

        // Then
        verify(workoutRepository, times(1)).findAllDefault(sort);
        assertEquals(2, responseWorkouts.size());
        assertThat(defaultWorkouts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "bodyParts", "user")
                .isEqualTo(responseWorkouts);
    }

    @Test
    void getCustomWorkoutByIdTest_shouldReturnCustomWorkout() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Workout customWorkout1 = testUtil.createCustomWorkout(1, List.of(defaultExercise1), user);
        Workout defaultWorkout1 = testUtil.createDefaultWorkout(2, List.of(defaultExercise1));

        when(workoutRepository.findById(customWorkout1.getId())).thenReturn(Optional.of(customWorkout1));

        // When
        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(customWorkout1.getId(), true);

        // Then
        verify(workoutRepository, times(1)).findById(customWorkout1.getId());
        assertThat(customWorkout1)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "user")
                .isEqualTo(responseWorkout);
    }

    @Test
    void getCustomWorkoutByIdTest_shouldThrowCustomWorkoutRequired_whenWorkoutIsDefault() {
        // Given
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                testUtil.createDefaultExercise(1, true, List.of(bodyPart1), List.of(defaultHttpRef1));
        Workout defaultWorkout1 = testUtil.createDefaultWorkout(1, List.of(defaultExercise1));

        when(workoutRepository.findById(defaultWorkout1.getId())).thenReturn(Optional.of(defaultWorkout1));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.getWorkoutById(defaultWorkout1.getId(), true);
        });

        // Then
        verify(workoutRepository, times(1)).findById(defaultWorkout1.getId());
        assertEquals(ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldReturnWorkoutResponseDto_whenValidRequestDtoProvided() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise), user);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(customExercise.getId()));

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(workoutRepository.findById(customWorkout.getId())).thenReturn(Optional.of(customWorkout));
        when(workoutRepository.save(any(Workout.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));

        // When
        WorkoutResponseDto responseDto =
                workoutService.updateCustomWorkout(user.getId(), customWorkout.getId(), requestDto);

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(customWorkout.getId());
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(workoutRepository, times(1)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(defaultExercise.getId());
        verify(exerciseRepository, times(1)).findById(customExercise.getId());

        assertEquals(customWorkout.getId(), responseDto.getId());
        assertEquals(requestDto.getTitle(), responseDto.getTitle());
        assertEquals(requestDto.getDescription(), responseDto.getDescription());
        assertTrue(responseDto.isCustom());
        assertTrue(responseDto.isNeedsEquipment());

        assertThat(responseDto.getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(List.of(bodyPart2));

        assertThat(responseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "user")
                .isEqualTo(List.of(customExercise));

        assertThat(responseDto.getExercises().get(0).getBodyParts())
                .usingRecursiveComparison()
                .ignoringFields("exercises")
                .isEqualTo(customExercise.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(0).getHttpRefs())
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(customExercise.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowEmptyRequestExceptionAnd400_whenEmptyDtoProvided() {
        // Given
        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
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
        User user = testUtil.createUser(1);
        long nonExistingWorkoutId = 1000L;

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(workoutRepository.findById(nonExistingWorkoutId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), nonExistingWorkoutId, requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(nonExistingWorkoutId);
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenUserDoesntHaveWorkouts() {
        // Given
        User user = testUtil.createUser(1);
        Workout workout = testUtil.createDefaultWorkout(1, Collections.emptyList());

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

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
        User user1 = testUtil.createUser(1);
        User user2 = testUtil.createUser(2);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user1);
        Workout customWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user1);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());

        when(userService.getUserById(user2.getId())).thenReturn(user2);
        when(workoutRepository.findById(customWorkout.getId())).thenReturn(Optional.of(customWorkout));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user2.getId(), customWorkout.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user2.getId());
        verify(workoutRepository, times(1)).findById(customWorkout.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowTitleDuplicateAnd400_whenWorkoutTitleDuplicated() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout alreadyExistsCustomWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise), user);
        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(2, List.of(customExercise), user);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
        requestDto.setTitle(alreadyExistsCustomWorkout.getTitle());

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
        when(workoutRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(List.of(alreadyExistsCustomWorkout));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), customWorkoutToUpdate.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(user.getId());
        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
        verify(workoutRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.TITLE_DUPLICATE.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowInvalidNestedObjectAnd400_whenExerciseNotFound() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        long nonExistingExerciseId = 1000L;
        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(1, List.of(customExercise, defaultExercise), user);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(nonExistingExerciseId));

        when(userService.getUserById(1)).thenReturn(user);
        when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user.getId(), customWorkoutToUpdate.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(nonExistingExerciseId);

        assertEquals(ErrorMessage.INVALID_NESTED_OBJECT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenExerciseDoesntBelongToUser() {
        // Given
        User user1 = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(1, List.of(defaultExercise), user1);

        User user2 = testUtil.createUser(2);
        Exercise customExerciseOfAnotherUser =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user2);

        WorkoutUpdateRequestDto requestDto =
                dtoUtil.workoutUpdateRequestDto(1, List.of(customExerciseOfAnotherUser.getId()));

        when(userService.getUserById(1)).thenReturn(user1);
        when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
        when(exerciseRepository.findById(customExerciseOfAnotherUser.getId()))
                .thenReturn(Optional.of(customExerciseOfAnotherUser));

        // When
        ApiException exception = assertThrows(ApiException.class, () -> {
            workoutService.updateCustomWorkout(user1.getId(), customWorkoutToUpdate.getId(), requestDto);
        });

        // Then
        verify(userService, times(1)).getUserById(1);
        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
        verify(workoutRepository, times(0)).save(any(Workout.class));
        verify(exerciseRepository, times(1)).findById(customExerciseOfAnotherUser.getId());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
    }

    @Test
    void deleteCustomWorkoutTest_shouldReturnDeletedId_whenValidUserIdAndWorkoutIdGiven() {
        // Given
        User user = testUtil.createUser(1);
        BodyPart bodyPart1 = testUtil.createBodyPart(1);
        BodyPart bodyPart2 = testUtil.createBodyPart(2);
        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkoutToDelete = testUtil.createCustomWorkout(1, List.of(customExercise, defaultExercise), user);

        when(workoutRepository.findCustomByWorkoutIdAndUserId(customWorkoutToDelete.getId(), user.getId()))
                .thenReturn(List.of(customWorkoutToDelete));

        // When
        workoutService.deleteCustomWorkout(user.getId(), customWorkoutToDelete.getId());

        // Then
        verify(workoutRepository, times(1)).findCustomByWorkoutIdAndUserId(customWorkoutToDelete.getId(), user.getId());
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
        assertEquals(ErrorMessage.REQUESTED_RESOURCE_NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }
}
