// package healthy.lifestyle.backend.workout.service;
//
// import static java.util.Objects.nonNull;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;
//
// import healthy.lifestyle.backend.activity.workout.dto.ExerciseCreateRequestDto;
// import healthy.lifestyle.backend.activity.workout.dto.ExerciseResponseDto;
// import healthy.lifestyle.backend.activity.workout.dto.ExerciseUpdateRequestDto;
// import healthy.lifestyle.backend.activity.workout.model.BodyPart;
// import healthy.lifestyle.backend.activity.workout.model.Exercise;
// import healthy.lifestyle.backend.activity.workout.model.HttpRef;
// import healthy.lifestyle.backend.activity.workout.repository.BodyPartRepository;
// import healthy.lifestyle.backend.activity.workout.repository.ExerciseRepository;
// import healthy.lifestyle.backend.activity.workout.repository.HttpRefRepository;
// import healthy.lifestyle.backend.activity.workout.service.ExerciseServiceImpl;
// import healthy.lifestyle.backend.shared.exception.ApiException;
// import healthy.lifestyle.backend.shared.exception.ApiExceptionCustomMessage;
// import healthy.lifestyle.backend.shared.exception.ErrorMessage;
// import healthy.lifestyle.backend.shared.util.VerificationUtil;
// import healthy.lifestyle.backend.user.model.Country;
// import healthy.lifestyle.backend.user.model.Role;
// import healthy.lifestyle.backend.user.model.User;
// import healthy.lifestyle.backend.user.service.UserServiceImpl;
// import healthy.lifestyle.backend.util.DtoUtil;
// import healthy.lifestyle.backend.util.TestUtil;
// import java.util.*;
// import java.util.stream.Stream;
// import org.assertj.core.api.Assertions;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.Spy;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.modelmapper.ModelMapper;
// import org.springframework.data.domain.Page;
// import org.springframework.http.HttpStatus;
//
// @ExtendWith(MockitoExtension.class)
// class ExerciseServiceTest {
//    @InjectMocks
//    ExerciseServiceImpl exerciseService;
//
//    @Mock
//    private ExerciseRepository exerciseRepository;
//
//    @Mock
//    private BodyPartRepository bodyPartRepository;
//
//    @Mock
//    private HttpRefRepository httpRefRepository;
//
//    @Mock
//    private UserServiceImpl userService;
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
//    void createExercise_shouldReturnCreatedDto() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = false;
//        ExerciseCreateRequestDto requestDto = dtoUtil.exerciseCreateRequestDto(
//                1,
//                needsEquipment,
//                List.of(bodyPart1.getId(), bodyPart2.getId()),
//                List.of(customHttpRef.getId(), defaultHttpRef.getId()));
//
//        when(bodyPartRepository.findById(bodyPart1.getId())).thenReturn(Optional.of(bodyPart1));
//        when(bodyPartRepository.findById(bodyPart2.getId())).thenReturn(Optional.of(bodyPart2));
//        when(httpRefRepository.findById(customHttpRef.getId())).thenReturn(Optional.of(customHttpRef));
//        when(httpRefRepository.findById(defaultHttpRef.getId())).thenReturn(Optional.of(defaultHttpRef));
//
//        when(exerciseRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(Collections.emptyList());
//        when(exerciseRepository.save(any(Exercise.class)))
//                .thenAnswer(invocation -> invocation.getArguments()[0]);
//
//        doNothing().when(userService).addExerciseToUser(any(Long.class), any(Exercise.class));
//
//        // When
//        ExerciseResponseDto exerciseActual = exerciseService.createCustomExercise(requestDto, user.getId());
//
//        // Then
//        verify(bodyPartRepository, times(2)).findById(anyLong());
//        verify(httpRefRepository, times(2)).findById(anyLong());
//        verify(exerciseRepository, times(1))
//                .findDefaultAndCustomByTitleAndUserId(eq(requestDto.getTitle()), eq(user.getId()));
//        verify(exerciseRepository, times(1)).save(any(Exercise.class));
//        verify(userService, times(1)).addExerciseToUser(eq(user.getId()), any());
//
//        assertThat(requestDto)
//                .usingRecursiveComparison()
//                .ignoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(exerciseActual);
//
//        assertThat(List.of(bodyPart1, bodyPart2))
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
//                .isEqualTo(exerciseActual.getBodyParts());
//
//        assertThat(List.of(customHttpRef, defaultHttpRef))
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals",
// "nutritions")
//                .isEqualTo(exerciseActual.getHttpRefs());
//    }
//
//    @ParameterizedTest
//    @MethodSource("getExercisesWithFilterValidFilters")
//    void getExercisesWithFilter_shouldReturnPage_whenValidFilters(Boolean isCustom, Long userId) {
//        // Given
//        int currentPageNumber = 0;
//        int itemsPerPage = 2;
//        String sortDirection = "ASC";
//        String sortField = "id";
//
//        if (isCustom == null) {
//            when(exerciseRepository.findDefaultAndCustomWithFilter(
//                            nullable(Long.class),
//                            nullable(String.class),
//                            nullable(String.class),
//                            nullable(Boolean.class),
//                            anyList(),
//                            any()))
//                    .thenReturn(Page.empty());
//        } else {
//            when(exerciseRepository.findDefaultOrCustomWithFilter(
//                            nullable(Boolean.class),
//                            nullable(Long.class),
//                            nullable(String.class),
//                            nullable(String.class),
//                            nullable(Boolean.class),
//                            anyList(),
//                            any()))
//                    .thenReturn(Page.empty());
//        }
//
//        // When
//        Page<ExerciseResponseDto> dtoPage = exerciseService.getExercisesWithFilter(
//                isCustom,
//                userId,
//                "title",
//                "desc",
//                true,
//                List.of(1L),
//                sortField,
//                sortDirection,
//                currentPageNumber,
//                itemsPerPage);
//
//        // Then
//        verify(bodyPartRepository, times(0)).findAll();
//        if (isCustom == null) {
//            verify(exerciseRepository, times(1))
//                    .findDefaultAndCustomWithFilter(
//                            nullable(Long.class),
//                            nullable(String.class),
//                            nullable(String.class),
//                            nullable(Boolean.class),
//                            anyList(),
//                            any());
//        } else {
//            verify(exerciseRepository, times(1))
//                    .findDefaultOrCustomWithFilter(
//                            nullable(Boolean.class),
//                            nullable(Long.class),
//                            nullable(String.class),
//                            nullable(String.class),
//                            nullable(Boolean.class),
//                            anyList(),
//                            any());
//        }
//    }
//
//    static Stream<Arguments> getExercisesWithFilterValidFilters() {
//        return Stream.of(Arguments.of(null, 1L), Arguments.of(true, 1L), Arguments.of(false, null));
//    }
//
//    @ParameterizedTest
//    @MethodSource("getExercisesWithFilterInvalidFilters")
//    void getExercisesWithFilter_shouldThrowErrorWith400_whenInvalidFilters(Boolean isCustom, Long userId) {
//        // Given
//        int currentPageNumber = 0;
//        int itemsPerPage = 2;
//        String sortDirection = "ASC";
//        String sortField = "id";
//        ApiExceptionCustomMessage expectedException =
//                new ApiExceptionCustomMessage("Invalid args combination", HttpStatus.BAD_REQUEST);
//
//        // When
//        ApiExceptionCustomMessage actualException = assertThrows(
//                ApiExceptionCustomMessage.class,
//                () -> exerciseService.getExercisesWithFilter(
//                        isCustom,
//                        userId,
//                        "title",
//                        "desc",
//                        true,
//                        List.of(1L),
//                        sortField,
//                        sortDirection,
//                        currentPageNumber,
//                        itemsPerPage));
//
//        // Then
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatus(), actualException.getHttpStatus());
//
//        verify(bodyPartRepository, times(0)).findAll();
//        verify(exerciseRepository, times(0))
//                .findDefaultAndCustomWithFilter(
//                        nullable(Long.class),
//                        nullable(String.class),
//                        nullable(String.class),
//                        nullable(Boolean.class),
//                        anyList(),
//                        any());
//        verify(exerciseRepository, times(0))
//                .findDefaultOrCustomWithFilter(
//                        nullable(Boolean.class),
//                        nullable(Long.class),
//                        nullable(String.class),
//                        nullable(String.class),
//                        nullable(Boolean.class),
//                        anyList(),
//                        any());
//    }
//
//    static Stream<Arguments> getExercisesWithFilterInvalidFilters() {
//        return Stream.of(Arguments.of(null, null), Arguments.of(false, 1L), Arguments.of(true, null));
//    }
//
//    @Test
//    void getExerciseById_shouldReturnDefaultExerciseDto() {
//        // Given
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        boolean needsEquipment = true;
//        Exercise defaultExercise =
//                testUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef));
//
//        when(exerciseRepository.findById(defaultExercise.getId())).thenReturn(Optional.of(defaultExercise));
//
//        // When
//        ExerciseResponseDto exerciseDtoActual = exerciseService.getExerciseById(defaultExercise.getId(), true, null);
//
//        // Then
//        verify((exerciseRepository), times(1)).findById(defaultExercise.getId());
//        verify(userService, times(0)).getUserById(anyLong());
//
//        assertThat(defaultExercise)
//                .usingRecursiveComparison()
//                .ignoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(exerciseDtoActual);
//
//        List<BodyPart> bodyParts_ = defaultExercise.getBodyParts().stream()
//                .sorted(Comparator.comparingLong(BodyPart::getId))
//                .toList();
//        assertThat(bodyParts_)
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
//                .isEqualTo(exerciseDtoActual.getBodyParts());
//
//        List<HttpRef> httpRefs_ = defaultExercise.getHttpRefs().stream()
//                .sorted(Comparator.comparingLong(HttpRef::getId))
//                .toList();
//        assertThat(httpRefs_)
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals",
// "nutritions")
//                .isEqualTo(exerciseDtoActual.getHttpRefs());
//    }
//
//    @Test
//    void getExerciseById_shouldReturnCustomExerciseDto() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef, customHttpRef), user);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(userService.getUserById(user.getId())).thenReturn(user);
//
//        // When
//        ExerciseResponseDto exerciseDtoActual =
//                exerciseService.getExerciseById(customExercise.getId(), false, user.getId());
//
//        // Then
//        verify((exerciseRepository), times(1)).findById(customExercise.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//
//        Assertions.assertThat(customExercise)
//                .usingRecursiveComparison()
//                .ignoringFields("user", "bodyParts", "httpRefs")
//                .isEqualTo(exerciseDtoActual);
//
//        List<BodyPart> bodyParts_ = customExercise.getBodyParts().stream()
//                .sorted(Comparator.comparingLong(BodyPart::getId))
//                .toList();
//        assertThat(bodyParts_)
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
//                .isEqualTo(exerciseDtoActual.getBodyParts());
//
//        List<HttpRef> httpRefs_ = customExercise.getHttpRefs().stream()
//                .sorted(Comparator.comparingLong(HttpRef::getId))
//                .toList();
//        assertThat(httpRefs_)
//                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user", "mentals",
// "nutritions")
//                .isEqualTo(exerciseDtoActual.getHttpRefs());
//    }
//
//    @Test
//    void getExerciseById_shouldThrowErrorWith404_whenExerciseNotFound() {
//        // Given
//        long nonExistingExerciseId = 1000L;
//        ApiException expectedException =
//                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistingExerciseId, HttpStatus.NOT_FOUND);
//        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class, () -> exerciseService.getExerciseById(nonExistingExerciseId, true, null));
//
//        // Then
//        verify((exerciseRepository), times(1)).findById(nonExistingExerciseId);
//        verify(userService, times(0)).getUserById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void getExerciseById_shouldThrowErrorWith400_whenDefaultExerciseRequestedInsteadOfCustom() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef, customHttpRef), user);
//        ApiException expectedException = new ApiException(
//                ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class, () -> exerciseService.getExerciseById(customExercise.getId(), true, null));
//
//        // Then
//        verify((exerciseRepository), times(1)).findById(customExercise.getId());
//        verify(userService, times(0)).getUserById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void getExerciseById_shouldThrowErrorWith400_whenExerciseUserMismatch() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(2, user);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef, customHttpRef), user);
//
//        User user2 = testUtil.createUser(2);
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, customExercise.getId(), HttpStatus.BAD_REQUEST);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(userService.getUserById(user2.getId())).thenReturn(user2);
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.getExerciseById(customExercise.getId(), false, user2.getId()));
//
//        // Then
//        verify((exerciseRepository), times(1)).findById(customExercise.getId());
//        verify(userService, times(1)).getUserById(user2.getId());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @ParameterizedTest
//    @MethodSource("updateCustomExerciseValidFields")
//    void updateCustomExercise_shouldReturnUpdatedDto_whenValidFields(
//            String updateTitle, String updateDescription, Boolean updateNeedsEquipment)
//            throws NoSuchFieldException, IllegalAccessException {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart1 = testUtil.createBodyPart(1);
//        BodyPart bodyPart2 = testUtil.createBodyPart(2);
//        HttpRef customHttpRef1 = testUtil.createCustomHttpRef(1, user);
//        HttpRef customHttpRef2 = testUtil.createCustomHttpRef(2, user);
//        HttpRef defaultHttpRef1 = testUtil.createDefaultHttpRef(3);
//        HttpRef defaultHttpRef2 = testUtil.createDefaultHttpRef(4);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1,
//                needsEquipment,
//                List.of(bodyPart1, bodyPart2),
//                List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2),
//                user);
//
//        BodyPart bodyPart3 = testUtil.createBodyPart(3);
//        HttpRef customHttpRef3 = testUtil.createCustomHttpRef(5, user);
//        HttpRef defaultHttpRef3 = testUtil.createDefaultHttpRef(6);
//        needsEquipment = false;
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDto(
//                2,
//                needsEquipment,
//                List.of(bodyPart1.getId(), bodyPart3.getId()),
//                List.of(
//                        customHttpRef1.getId(),
//                        defaultHttpRef1.getId(),
//                        customHttpRef3.getId(),
//                        defaultHttpRef3.getId()));
//        requestDto.setTitle(updateTitle);
//        requestDto.setDescription(updateDescription);
//        requestDto.setNeedsEquipment(updateNeedsEquipment);
//
//        String initialTitle = customExercise.getTitle();
//        String initialDescription = customExercise.getDescription();
//        boolean initialNeedsEquipment = customExercise.isNeedsEquipment();
//
//        // Expected nested objects
//        List<BodyPart> expectedBodyParts = List.of(bodyPart1, bodyPart3);
//        List<HttpRef> expectedHttpRefs = List.of(customHttpRef1, defaultHttpRef1, customHttpRef3, defaultHttpRef3);
//
//        // Mocking
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(userService.getUserById(user.getId())).thenReturn(user);
//
//        if (nonNull(updateTitle)) {
//            when(exerciseRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                    .thenReturn(Collections.emptyList());
//        }
//
//        when(bodyPartRepository.findById(bodyPart2.getId())).thenReturn(Optional.of(bodyPart2));
//        when(bodyPartRepository.findById(bodyPart3.getId())).thenReturn(Optional.of(bodyPart3));
//        when(httpRefRepository.findById(customHttpRef2.getId())).thenReturn(Optional.of(customHttpRef2));
//        when(httpRefRepository.findById(customHttpRef3.getId())).thenReturn(Optional.of(customHttpRef3));
//        when(httpRefRepository.findById(defaultHttpRef2.getId())).thenReturn(Optional.of(defaultHttpRef2));
//        when(httpRefRepository.findById(defaultHttpRef3.getId())).thenReturn(Optional.of(defaultHttpRef3));
//        when(exerciseRepository.save(any(Exercise.class)))
//                .thenAnswer(invocation -> invocation.getArguments()[0]);
//
//        // When
//        ExerciseResponseDto responseDto =
//                exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto);
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//
//        if (nonNull(updateTitle)) {
//            verify(exerciseRepository, times(1))
//                    .findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        }
//        verify(bodyPartRepository, times(2)).findById(anyLong());
//        verify(httpRefRepository, times(4)).findById(anyLong());
//        verify(exerciseRepository, times(1)).save(any(Exercise.class));
//
//        assertEquals(customExercise.getId(), responseDto.getId());
//        assertTrue(responseDto.isCustom());
//
//        if (nonNull(updateTitle)) assertEquals(requestDto.getTitle(), responseDto.getTitle());
//        else assertEquals(initialTitle, responseDto.getTitle());
//
//        if (nonNull(updateDescription)) assertEquals(requestDto.getDescription(), responseDto.getDescription());
//        else assertEquals(initialDescription, responseDto.getDescription());
//
//        if (nonNull(updateNeedsEquipment)) assertEquals(requestDto.getNeedsEquipment(),
// responseDto.isNeedsEquipment());
//        else assertEquals(initialNeedsEquipment, responseDto.isNeedsEquipment());
//
//        assertThat(responseDto.getBodyParts()).usingRecursiveComparison().isEqualTo(expectedBodyParts);
//        assertThat(responseDto.getHttpRefs()).usingRecursiveComparison().isEqualTo(expectedHttpRefs);
//    }
//
//    static Stream<Arguments> updateCustomExerciseValidFields() {
//        return Stream.of(
//                Arguments.of("Update title", "Update description", false),
//                Arguments.of("Update title", "Update description", null),
//                Arguments.of("Update title", null, false),
//                Arguments.of(null, "Update description", false),
//                Arguments.of(null, "Update description", null),
//                Arguments.of(null, null, false));
//    }
//
//    @Test
//    void updateCustomExercise_shouldReturnUpdatedDto_whenHttpRefsIdsNotGiven()
//            throws NoSuchFieldException, IllegalAccessException {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//
//        String initialTitle = customExercise.getTitle();
//        String initialDescription = customExercise.getDescription();
//        boolean initialNeedsEquipment = customExercise.isNeedsEquipment();
//
//        // Update DTO, http refs should be removed from the target exercise. Other fields should remain the same.
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        List<Long> newHttpRefs = Collections.emptyList();
//        requestDto.setHttpRefIds(newHttpRefs);
//        requestDto.setBodyPartIds(customExercise.getBodyPartsIdsSorted());
//
//        // Mocking
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(userService.getUserById(user.getId())).thenReturn(user);
//        when(exerciseRepository.save(any(Exercise.class)))
//                .thenAnswer(invocation -> invocation.getArguments()[0]);
//
//        // When
//        ExerciseResponseDto responseDto =
//                exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto);
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//        verify(exerciseRepository, times(1)).save(any(Exercise.class));
//
//        assertEquals(customExercise.getId(), responseDto.getId());
//        assertTrue(responseDto.isCustom());
//        assertEquals(initialTitle, responseDto.getTitle());
//        assertEquals(initialDescription, responseDto.getDescription());
//        assertEquals(initialNeedsEquipment, responseDto.isNeedsEquipment());
//
//        assertThat(responseDto.getBodyParts()).usingRecursiveComparison().isEqualTo(List.of(bodyPart));
//        assertThat(responseDto.getHttpRefs()).usingRecursiveComparison().isEqualTo(Collections.emptyList());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith400_whenNoUpdatesRequest() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(customExercise.getBodyPartsIdsSorted());
//        requestDto.setHttpRefIds(customExercise.getHttpRefsIdsSorted());
//
//        ApiExceptionCustomMessage expectedException =
//                new ApiExceptionCustomMessage(ErrorMessage.NO_UPDATES_REQUEST.getName(), HttpStatus.BAD_REQUEST);
//
//        // Mocking
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//
//        // When
//        ApiExceptionCustomMessage actualException = assertThrows(
//                ApiExceptionCustomMessage.class,
//                () -> exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(userService, times(0)).getUserById(anyLong());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith404_whenExerciseNotFound() {
//        // Given
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(List.of(1L));
//        requestDto.setHttpRefIds(List.of(1L));
//        long nonExistingExerciseId = 1000L;
//        long nonExistingUserId = 1000L;
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistingExerciseId, HttpStatus.NOT_FOUND);
//
//        when(exerciseRepository.findById(nonExistingExerciseId)).thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.updateCustomExercise(nonExistingExerciseId, nonExistingUserId, requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(nonExistingExerciseId);
//        verify(userService, times(0)).getUserById(anyLong());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith400_whenExerciseUserMismatch() {
//        // Given
//        Role role = testUtil.createUserRole();
//        Country country = testUtil.createCountry(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//
//        User user1 = testUtil.createUser(1, role, country);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user1);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise1 = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user1);
//
//        User user2 = testUtil.createUser(2, role, country);
//        Exercise customExercise2 =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef), user2);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(customExercise1.getBodyPartsIdsSorted());
//        requestDto.setHttpRefIds(Collections.emptyList());
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, customExercise1.getId(),
// HttpStatus.BAD_REQUEST);
//
//        when(exerciseRepository.findById(customExercise1.getId())).thenReturn(Optional.of(customExercise1));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.updateCustomExercise(customExercise1.getId(), user2.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise1.getId());
//        verify(userService, times(0)).getUserById(user2.getId());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith400_whenExerciseWithNewTitleAlreadyExists() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//        Exercise alreadyExistedCustomExercise =
//                testUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart), Collections.emptyList(), user);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(customExercise.getBodyPartsIdsSorted());
//        requestDto.setHttpRefIds(Collections.emptyList());
//        requestDto.setTitle(alreadyExistedCustomExercise.getTitle());
//
//        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(exerciseRepository.findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
//                .thenReturn(List.of(alreadyExistedCustomExercise));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(exerciseRepository, times(1)).findDefaultAndCustomByTitleAndUserId(requestDto.getTitle(),
// user.getId());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessage(), actualException.getMessage());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith404_whenBodyPartNotFound() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        long nonExistingBodyPartId = 1000L;
//        requestDto.setBodyPartIds(List.of(nonExistingBodyPartId));
//        requestDto.setHttpRefIds(Collections.emptyList());
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.BODY_PART_NOT_FOUND, nonExistingBodyPartId, HttpStatus.NOT_FOUND);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
//        verify(bodyPartRepository, times(1)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @Test
//    void updateCustomExercise_shouldThrowErrorWith400_whenHttpRefUserMismatch() {
//        // Given
//        Role role = testUtil.createUserRole();
//        Country country = testUtil.createCountry(1);
//        User user = testUtil.createUser(1, role, country);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//
//        User user2 = testUtil.createUser(2, role, country);
//        HttpRef customHttpRef2 = testUtil.createCustomHttpRef(3, user2);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(customExercise.getBodyPartsIdsSorted());
//        requestDto.setHttpRefIds(List.of(customHttpRef2.getId()));
//
//        ApiException expectedException =
//                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, customHttpRef2.getId(), HttpStatus.BAD_REQUEST);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//        when(userService.getUserById(user.getId())).thenReturn(user);
//        when(httpRefRepository.findById(customHttpRef2.getId())).thenReturn(Optional.of(customHttpRef2));
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class,
//                () -> exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(userService, times(1)).getUserById(user.getId());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(1)).findById(anyLong());
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
//
//    @ParameterizedTest
//    @MethodSource("updateCustomExerciseFieldsAreNotDifferent")
//    void updateCustomExercise_shouldThrowErrorWith400_whenFieldsAreNotDifferent(
//            String updateTitle, String updateDescription, Boolean updateNeedsEquipment) {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise =
//                testUtil.createCustomExercise(1, true, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef),
// user);
//
//        ExerciseUpdateRequestDto requestDto = dtoUtil.exerciseUpdateRequestDtoEmpty();
//        requestDto.setBodyPartIds(customExercise.getBodyPartsIdsSorted());
//        requestDto.setHttpRefIds(Collections.emptyList());
//        requestDto.setTitle(updateTitle);
//        requestDto.setDescription(updateDescription);
//        requestDto.setNeedsEquipment(updateNeedsEquipment);
//
//        when(exerciseRepository.findById(customExercise.getId())).thenReturn(Optional.of(customExercise));
//
//        // When
//        ApiExceptionCustomMessage exception = assertThrows(
//                ApiExceptionCustomMessage.class,
//                () -> exerciseService.updateCustomExercise(customExercise.getId(), user.getId(), requestDto));
//
//        // Then
//        verify(exerciseRepository, times(1)).findById(customExercise.getId());
//        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
//        verify(bodyPartRepository, times(0)).findById(anyLong());
//        verify(httpRefRepository, times(0)).findById(anyLong());
//
//        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
//
//        if (nonNull(updateTitle))
//            assertEquals(ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + "title", exception.getMessage());
//        if (nonNull(updateDescription))
//            assertEquals(
//                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + "description", exception.getMessage());
//        if (nonNull(updateNeedsEquipment))
//            assertEquals(
//                    ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + "needsEquipment",
// exception.getMessage());
//    }
//
//    static Stream<Arguments> updateCustomExerciseFieldsAreNotDifferent() {
//        return Stream.of(
//                Arguments.of("Exercise 1", null, null),
//                Arguments.of(null, "Description 1", null),
//                Arguments.of(null, null, true));
//    }
//
//    @Test
//    void deleteCustomExercise_shouldReturnVoid_whenValidRequest() {
//        // Given
//        User user = testUtil.createUser(1);
//        BodyPart bodyPart = testUtil.createBodyPart(1);
//        HttpRef customHttpRef = testUtil.createCustomHttpRef(1, user);
//        HttpRef defaultHttpRef = testUtil.createDefaultHttpRef(2);
//        boolean needsEquipment = true;
//        Exercise customExercise = testUtil.createCustomExercise(
//                1, needsEquipment, List.of(bodyPart), List.of(customHttpRef, defaultHttpRef), user);
//        long exerciseId = customExercise.getId();
//
//        when(exerciseRepository.findCustomByExerciseIdAndUserId(customExercise.getId(), user.getId()))
//                .thenReturn(Optional.ofNullable(customExercise));
//        doNothing().when(userService).deleteExerciseFromUser(eq(user.getId()), any(Exercise.class));
//        doNothing().when(exerciseRepository).delete(any(Exercise.class));
//
//        // When
//        exerciseService.deleteCustomExercise(customExercise.getId(), user.getId());
//
//        // Then
//        verify(exerciseRepository, times(1)).findCustomByExerciseIdAndUserId(customExercise.getId(), user.getId());
//        verify(userService, times(1)).deleteExerciseFromUser(eq(user.getId()), any(Exercise.class));
//        verify(exerciseRepository, times(1)).delete(any(Exercise.class));
//    }
//
//    @Test
//    void deleteCustomExercise_shouldThrowErrorWith404_whenExerciseNotFound() {
//        // Given
//        long randomUserId = 1000L;
//        long nonExistentExerciseId = 1000L;
//        ApiException expectedException =
//                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistentExerciseId, HttpStatus.NOT_FOUND);
//
//        when(exerciseRepository.findCustomByExerciseIdAndUserId(nonExistentExerciseId, randomUserId))
//                .thenReturn(Optional.empty());
//
//        // When
//        ApiException actualException = assertThrows(
//                ApiException.class, () -> exerciseService.deleteCustomExercise(nonExistentExerciseId, randomUserId));
//
//        // Then
//        verify(exerciseRepository, times(1)).findCustomByExerciseIdAndUserId(nonExistentExerciseId, randomUserId);
//        verify(exerciseRepository, times(0)).delete(any(Exercise.class));
//
//        assertEquals(expectedException.getMessageWithResourceId(), actualException.getMessageWithResourceId());
//        assertEquals(expectedException.getHttpStatusValue(), actualException.getHttpStatusValue());
//    }
// }
