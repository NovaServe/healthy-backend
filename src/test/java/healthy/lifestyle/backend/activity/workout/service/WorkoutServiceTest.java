// package healthy.lifestyle.backend.workout.service;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;
//
// import healthy.lifestyle.backend.activity.workout.dto.WorkoutCreateRequestDto;
// import healthy.lifestyle.backend.activity.workout.dto.WorkoutResponseDto;
// import healthy.lifestyle.backend.activity.workout.dto.WorkoutUpdateRequestDto;
// import healthy.lifestyle.backend.activity.workout.model.BodyPart;
// import healthy.lifestyle.backend.activity.workout.model.Exercise;
// import healthy.lifestyle.backend.activity.workout.model.HttpRef;
// import healthy.lifestyle.backend.activity.workout.model.Workout;
// import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
// import healthy.lifestyle.backend.activity.workout.repository.WorkoutRepository;
// import healthy.lifestyle.backend.activity.workout.service.WorkoutServiceImpl;
// import healthy.lifestyle.backend.shared.exception.ApiException;
// import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
// import healthy.lifestyle.backend.shared.exception.ErrorMessage;
// import healthy.lifestyle.backend.shared.util.VerificationUtil;
// import healthy.lifestyle.backend.user.model.User;
// import healthy.lifestyle.backend.user.service.UserServiceImpl;
// import healthy.lifestyle.backend.util.DtoUtil;
// import healthy.lifestyle.backend.util.TestUtil;
// import java.util.*;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Spy;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.modelmapper.ModelMapper;
// import org.springframework.http.HttpStatus;
//
// @ExtendWith(MockitoExtension.class)
// class WorkoutServiceTest {
//    @InjectMocks
//    WorkoutServiceImpl workoutService;
//
//    @Mock
//    WorkoutRepository workoutRepository;
//
//    @Mock
//    UserServiceImpl userService;
//
//    @Mock
//    ExerciseRepository exerciseRepository;
//
//    @Spy
//    ModelMapper modelMapper;
//
//    @Spy
//    VerificationUtil verificationUtil;
//
//    TestUtil testUtil = new TestUtil();
//
//    DtoUtil dtoUtil = new DtoUtil();
//
//    @Test
//    void createCustomWorkout_shouldReturnCreatedDto_whenValidFields() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//
//        WorkoutCreateRequestDto requestDto =
//                dtoUtil.workoutCreateRequestDto(1, List.of(customExercise.getId(), defaultExercise.getId()));
//
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(Collections.emptyList());
//        when(userService.getUserById(user.getId())).thenReturn(user);
//        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(workoutRepository.save(any(Workout.class))).thenAnswer(invocation -> {
//            Workout saved = invocation.getArgument(0);
//            saved.setId(1L);
//            return saved;
//        });
//        doNothing().when(userService).addWorkoutToUser(any(User.class), any(Workout.class));
//
//        // When
//        WorkoutResponseDto workoutResponseDto = workoutService.createCustomWorkout(user.getId(), requestDto);
//
//        // Then
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//        verify(exerciseRepository, times(2)).findById(anyLong());
//        verify(workoutRepository, times(1)).save(any(Workout.class));
//        verify(userService, times(1)).addWorkoutToUser(any(User.class), any(Workout.class));
//
//        assertEquals(1L, workoutResponseDto.getId());
//        assertEquals(requestDto.getTitle(), workoutResponseDto.getTitle());
//        assertEquals(requestDto.getDescription(), workoutResponseDto.getDescription());
//        assertTrue(workoutResponseDto.isCustom());
//        assertTrue(workoutResponseDto.isNeedsEquipment());
//        assertEquals(2, workoutResponseDto.getExercises().size());
//        assertEquals(2, workoutResponseDto.getBodyParts().size());
//    }
//
//    @Test
//    void createCustomWorkout_shouldThrowErrorWith400_whenAlreadyExistsWithSameTitle() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        Workout alreadyExistedWorkout = testUtil.createCustomWorkout(1, List.of(customExercise), user);
//
//        WorkoutCreateRequestDto requestDto =
//                dtoUtil.workoutCreateRequestDto(1, List.of(customExercise.getId(), defaultExercise.getId()));
//        requestDto.setTitle(alreadyExistedWorkout.getTitle());
//
//        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(List.of(alreadyExistedWorkout));
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.createCustomWorkout(user.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(userService, times(0)).getUserById(user.getId());
//        verify(exerciseRepository, times(0)).findById(anyLong());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(userService, times(0)).addWorkoutToUser(any(User.class), any(Workout.class));
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void createCustomWorkout_shouldThrowErrorWith404_whenExerciseNotFound() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        long nonExistingExerciseId = customExercise.getId() + 1;
//
//        WorkoutCreateRequestDto requestDto = dtoUtil.workoutCreateRequestDto(
//                1, List.of(defaultExercise.getId(), customExercise.getId(), nonExistingExerciseId));
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistingExerciseId, HttpStatus.NOT_FOUND);
//
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(Collections.emptyList());
//        when(userService.getUserById(user.getId())).thenReturn(user);
//        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.createCustomWorkout(user.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//        verify(exerciseRepository, times(3)).findById(anyLong());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(userService, times(0)).addWorkoutToUser(any(User.class), any(Workout.class));
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void createCustomWorkout_shouldThrowErrorWith400_whenExerciseUserMismatch() {
//        // Given
//        User user1 = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef1 = testUtil.createCustomHttpRef(2, user1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise1 =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user1);
//
//        User user2 = testUtil.createUser(2);
//        HttpRef customHttpRef2 = testUtil.createCustomHttpRef(3, user2);
//        Exercise customExercise2 =
//                testUtil.createCustomExercise(3, needsEquipment, List.of(bodyPart2), List.of(customHttpRef2), user2);
//
//        WorkoutCreateRequestDto requestDto = dtoUtil.workoutCreateRequestDto(
//                1, List.of(defaultExercise.getId(), customExercise1.getId(), customExercise2.getId()));
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, customExercise2.getId(),
// HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user1.getId()))
//                .thenReturn(Collections.emptyList());
//        when(userService.getUserById(user1.getId())).thenReturn(user1);
//        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
//        when(exerciseRepository.findById(customExercise1.getId())).thenReturn(Optional.of(customExercise1));
//        when(exerciseRepository.findById(customExercise2.getId())).thenReturn(Optional.of(customExercise2));
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.createCustomWorkout(user1.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(),
// user1.getId());
//        verify(userService, times(1)).getUserById(user1.getId());
//        verify(exerciseRepository, times(3)).findById(anyLong());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(userService, times(0)).addWorkoutToUser(any(User.class), any(Workout.class));
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void getDefaultWorkoutById_shouldReturnDto_whenValidId() {
//        // Given
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise1 =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
//        Workout defaultWorkout1 = testUtil.createDefaultWorkout(1, List.of(defaultExercise1));
//
//        when(workoutRepository.findById(defaultWorkout1.getId())).thenReturn(Optional.of(defaultWorkout1));
//
//        // When
//        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(defaultWorkout1.getId(), false);
//
//        // Then
//        verify(workoutRepository, times(1)).findById(defaultWorkout1.getId());
//        assertThat(defaultWorkout1)
//                .usingRecursiveComparison()
//                .ignoringFields("exercises", "bodyParts", "user")
//                .isEqualTo(responseWorkout);
//    }
//
//    @Test
//    void getDefaultWorkoutById_shouldThrowErrorWith404_whenNotFound() {
//        // Given
//        long nonExistentWorkoutId = 1000L;
//        ApiException expectedException =
//                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistentWorkoutId, HttpStatus.NOT_FOUND);
//        when(workoutRepository.findById(nonExistentWorkoutId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.getWorkoutById(nonExistentWorkoutId, false));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(nonExistentWorkoutId);
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void getDefaultWorkoutById_shouldThrowErrorWith400_whenCustomWorkoutRequested() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise1 =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
//        Workout customWorkout1 = testUtil.createCustomWorkout(1, List.of(defaultExercise1), user);
//
//        ApiException expectedException = new ApiException(
//                ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findById(customWorkout1.getId())).thenReturn(Optional.of(customWorkout1));
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.getWorkoutById(customWorkout1.getId(), false));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkout1.getId());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void getCustomWorkoutById_shouldReturnDto_whenValidId() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise1 =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
//        Workout customWorkout1 = testUtil.createCustomWorkout(1, List.of(defaultExercise1), user);
//        Workout defaultWorkout1 = testUtil.createDefaultWorkout(2, List.of(defaultExercise1));
//
//        when(workoutRepository.findById(customWorkout1.getId())).thenReturn(Optional.of(customWorkout1));
//
//        // When
//        WorkoutResponseDto responseWorkout = workoutService.getWorkoutById(customWorkout1.getId(), true);
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkout1.getId());
//        assertThat(customWorkout1)
//                .usingRecursiveComparison()
//                .ignoringFields("exercises", "bodyParts", "user")
//                .isEqualTo(responseWorkout);
//    }
//
//    @Test
//    void getCustomWorkoutById_shouldThrowErrorWith400_whenDefaultWorkoutRequested() {
//        // Given
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise1 =
//                testUtil.createDefaultExercise(1, true, List.of(bodyPart1), List.of(defaultHttpRef1));
//        Workout defaultWorkout1 = testUtil.createDefaultWorkout(1, List.of(defaultExercise1));
//
//        ApiException expectedException = new ApiException(
//                ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findById(defaultWorkout1.getId())).thenReturn(Optional.of(defaultWorkout1));
//
//        // When
//        ApiException actualException =
//                assertThrows(ApiException.class, () -> workoutService.getWorkoutById(defaultWorkout1.getId(), true));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(defaultWorkout1.getId());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldReturnUpdatedDto_whenValidFields()
//            throws NoSuchFieldException, IllegalAccessException {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        Workout customWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise), user);
//
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(customExercise.getId()));
//
//        when(workoutRepository.findById(customWorkout.getId())).thenReturn(Optional.of(customWorkout));
//        when(workoutRepository.save(any(Workout.class)))
//                .thenAnswer(invocation -> invocation.getArguments()[0]);
//        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(Collections.emptyList());
//
//        // When
//        WorkoutResponseDto responseDto =
//                workoutService.updateCustomWorkout(user.getId(), customWorkout.getId(), requestDto);
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkout.getId());
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(workoutRepository, times(1)).save(any(Workout.class));
//        verify(exerciseRepository, times(1)).findById(defaultExercise.getId());
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//
//        assertEquals(customWorkout.getId(), responseDto.getId());
//        assertEquals(requestDto.getTitle(), responseDto.getTitle());
//        assertEquals(requestDto.getDescription(), responseDto.getDescription());
//        assertTrue(responseDto.isCustom());
//        assertTrue(responseDto.isNeedsEquipment());
//
//        assertThat(responseDto.getBodyParts())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
//                .isEqualTo(List.of(bodyPart2));
//
//        assertThat(responseDto.getExercises())
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "user")
//                .isEqualTo(List.of(customExercise));
//
//        assertThat(responseDto.getExercises().get(0).getBodyParts())
//                .usingRecursiveComparison()
//                .ignoringFields("exercises")
//                .isEqualTo(customExercise.getBodyParts().stream()
//                        .sorted(Comparator.comparingLong(BodyPart::getId))
//                        .toList());
//
//        assertThat(responseDto.getExercises().get(0).getHttpRefs())
//                .usingRecursiveComparison()
//                .ignoringFields("exercises", "user")
//                .isEqualTo(customExercise.getHttpRefs().stream()
//                        .sorted(Comparator.comparingLong(HttpRef::getId))
//                        .toList());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith400_whenEmptyFields() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        Exercise defaultExercise = testUtil.createDefaultExercise(1, false, List.of(bodyPart),
// Collections.emptyList());
//        Workout customWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise), user);
//        when(workoutRepository.findById(anyLong())).thenReturn(Optional.ofNullable(customWorkout));
//
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(defaultExercise.getId()));
//        requestDto.setTitle(null);
//        requestDto.setDescription(null);
//
//        ApiExceptionCustomMessage expectedException =
//                new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
//
//        // When
//        ApiExceptionCustomMessage actualException = assertThrows(
//                ApiExceptionCustomMessage.class, () -> workoutService.updateCustomWorkout(1L, 2L, requestDto));
//
//        // Then
//        verify(userService, times(0)).getUserById(1);
//        verify(workoutRepository, times(1)).findById(anyLong());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith404_whenNotFound() {
//        // Given
//        User user = testUtil.createUser(1);
//        long nonExistingWorkoutId = 1000L;
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
//        ApiException expectedException =
//                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistingWorkoutId, HttpStatus.NOT_FOUND);
//
//        when(workoutRepository.findById(nonExistingWorkoutId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(ApiException.class, () -> {
//            workoutService.updateCustomWorkout(user.getId(), nonExistingWorkoutId, requestDto);
//        });
//
//        // Then
//        verify(workoutRepository, times(1)).findById(nonExistingWorkoutId);
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith400_whenDefaultWorkoutRequested() {
//        // Given
//        User user = testUtil.createUser(1);
//        Workout workout = testUtil.createDefaultWorkout(1, Collections.emptyList());
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
//        ApiException expectedException =
//                new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null,
// HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> workoutService.updateCustomWorkout(user.getId(), workout.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(workout.getId());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith400_whenWorkoutUserMismatch() {
//        // Given
//        User user1 = testUtil.createUser(1);
//        User user2 = testUtil.createUser(2);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user1);
//        Workout customWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user1);
//
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, customWorkout.getId(), HttpStatus.BAD_REQUEST);
//
//        when(workoutRepository.findById(customWorkout.getId())).thenReturn(Optional.of(customWorkout));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> workoutService.updateCustomWorkout(user2.getId(), customWorkout.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkout.getId());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith400_whenWorkoutTitleDuplicated() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        Workout alreadyExistsCustomWorkout = testUtil.createCustomWorkout(1, List.of(defaultExercise), user);
//        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(2, List.of(customExercise), user);
//
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, Collections.emptyList());
//        requestDto.setTitle(alreadyExistsCustomWorkout.getTitle());
//
//        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
//
//
// when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
//        when(workoutRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(List.of(alreadyExistsCustomWorkout));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> workoutService.updateCustomWorkout(user.getId(), customWorkoutToUpdate.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
//        verify(workoutRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith404_whenExerciseNotFound() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        long nonExistingExerciseId = 1000L;
//        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(1, List.of(customExercise, defaultExercise),
// user);
//
//        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(nonExistingExerciseId));
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistingExerciseId, HttpStatus.NOT_FOUND);
//
//
// when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
//        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> workoutService.updateCustomWorkout(user.getId(), customWorkoutToUpdate.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(1)).findById(nonExistingExerciseId);
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomWorkout_shouldThrowErrorWith400_whenExerciseUserMismatch() {
//        // Given
//        User user1 = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Workout customWorkoutToUpdate = testUtil.createCustomWorkout(1, List.of(defaultExercise), user1);
//
//        User user2 = testUtil.createUser(2);
//        Exercise customExerciseOfAnotherUser =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user2);
//
//        WorkoutUpdateRequestDto requestDto =
//                dtoUtil.workoutUpdateRequestDto(1, List.of(customExerciseOfAnotherUser.getId()));
//
//        ApiException expectedException = new ApiException(
//                ErrorMessage.USER_EXERCISE_MISMATCH, customExerciseOfAnotherUser.getId(), HttpStatus.BAD_REQUEST);
//
//
// when(workoutRepository.findById(customWorkoutToUpdate.getId())).thenReturn(Optional.of(customWorkoutToUpdate));
//        when(exerciseRepository.findById(customExerciseOfAnotherUser.getId()))
//                .thenReturn(Optional.of(customExerciseOfAnotherUser));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> workoutService.updateCustomWorkout(user1.getId(), customWorkoutToUpdate.getId(), requestDto));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkoutToUpdate.getId());
//        verify(workoutRepository, times(0)).save(any(Workout.class));
//        verify(exerciseRepository, times(1)).findById(customExerciseOfAnotherUser.getId());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void deleteCustomWorkout_shouldReturnDeletedId_whenValidRequest() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
//        Exercise customExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
//        Workout customWorkoutToDelete = testUtil.createCustomWorkout(1, List.of(customExercise, defaultExercise),
// user);
//
//
// when(workoutRepository.findById(customWorkoutToDelete.getId())).thenReturn(Optional.of(customWorkoutToDelete));
//
//        // When
//        workoutService.deleteCustomWorkout(user.getId(), customWorkoutToDelete.getId());
//
//        // Then
//        verify(workoutRepository, times(1)).findById(customWorkoutToDelete.getId());
//    }
//
//    @Test
//    void deleteCustomWorkout_shouldThrowErrorWith404_whenNotFound() {
//        // Given
//        long randomUserId = 1;
//        long nonExistentWorkoutId = 2;
//        ApiException expectedException =
//                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistentWorkoutId, HttpStatus.NOT_FOUND);
//
//        when(workoutRepository.findById(nonExistentWorkoutId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class, () -> workoutService.deleteCustomWorkout(randomUserId, nonExistentWorkoutId));
//
//        // Then
//        verify(workoutRepository, times(1)).findById(nonExistentWorkoutId);
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
// }
