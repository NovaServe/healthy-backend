package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import healthy.lifestyle.backend.data.*;
import healthy.lifestyle.backend.data.bodypart.BodyPartTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseDtoTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseTestBuilder;
import healthy.lifestyle.backend.data.httpref.HttpRefTestBuilder;
import healthy.lifestyle.backend.data.user.UserTestBuilder;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceUpdateTest {
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

    UserTestBuilder userTestBuilder = new UserTestBuilder();

    BodyPartTestBuilder bodyPartTestBuilder = new BodyPartTestBuilder();

    HttpRefTestBuilder mediaTestBuilder = new HttpRefTestBuilder();

    ExerciseDtoTestBuilder exerciseDtoTestBuilder = new ExerciseDtoTestBuilder();

    ExerciseTestBuilder exerciseTestBuilder = new ExerciseTestBuilder();

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultipleValidInputs")
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDto_whenValidRequestDtoGiven(
            String updateTitle, String updateDescription, Boolean updateNeedsEquipment)
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        // User with one exercise and related body parts and default media.
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        String initialTitle = userWrapper.getExerciseSingle().getTitle();
        String initialDescription = userWrapper.getExerciseSingle().getDescription();
        boolean initialNeedsEquipment = userWrapper.getExerciseSingle().isNeedsEquipment();

        // Custom and default medias to update existing exercise.
        HttpRefTestBuilder.HttpRefWrapper mediaCustomWrapper = mediaTestBuilder.getWrapper();
        mediaCustomWrapper
                .setIsCustom(true)
                .setIdOrSeed(10)
                .setUser(userWrapper.getUser())
                .buildSingle();
        userWrapper.addCustomHttpRefs(mediaCustomWrapper.getSingleAsList());

        HttpRefTestBuilder.HttpRefWrapper mediaDefaultWrapper = mediaTestBuilder.getWrapper();
        mediaDefaultWrapper.setIsCustom(false).setIdOrSeed(20).buildSingle();

        BodyPartTestBuilder.BodyPartWrapper bodyPartWrapper = bodyPartTestBuilder.getWrapper();
        bodyPartWrapper.setIdOrSeed(10).buildSingle();

        // Update DTO, nested entities should be added and removed as well.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);

        List<Long> newBodyPartsIds = List.of(
                userWrapper.getBodyPartIdByIndexFromSingleExercise(1),
                userWrapper.getBodyPartIdByIndexFromSingleExercise(2),
                bodyPartWrapper.getSingleId());
        List<Long> newHttpRefsIds = List.of(
                userWrapper.getHttpRefIdByIndexFromSingleExercise(1),
                userWrapper.getHttpRefIdByIndexFromSingleExercise(2),
                mediaCustomWrapper.getSingleId(),
                mediaDefaultWrapper.getSingleId());

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getExerciseIdSingle()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(newBodyPartsIds)
                .setMediasIds(newHttpRefsIds)
                .buildExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("title", updateTitle);
        requestDtoWrapper.setFieldValue("description", updateDescription);
        requestDtoWrapper.setFieldValue("needsEquipment", updateNeedsEquipment);

        // Expected nested objects
        List<BodyPart> expectedBodyParts = List.of(
                userWrapper.getBodyPartByIndexFromSingleExercise(1),
                userWrapper.getBodyPartByIndexFromSingleExercise(2),
                bodyPartWrapper.getSingle());
        List<HttpRef> expectedHttpRefs = List.of(
                userWrapper.getHttpRefByIndexFromSingleExercise(1),
                userWrapper.getHttpRefByIndexFromSingleExercise(2),
                mediaCustomWrapper.getSingle(),
                mediaDefaultWrapper.getSingle());

        // Mocking
        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId()))
                .thenReturn(Optional.of(userWrapper.getExerciseSingle()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());

        if (nonNull(updateTitle)) {
            when(exerciseRepository.findCustomByTitleAndUserId(
                            requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId()))
                    .thenReturn(Optional.empty());
        }

        when(bodyPartRepository.findById(userWrapper.getBodyPartIdByIndexFromSingleExercise(0)))
                .thenReturn(Optional.ofNullable(userWrapper.getBodyPartByIndexFromSingleExercise(0)));
        when(bodyPartRepository.findById(userWrapper.getBodyPartIdByIndexFromSingleExercise(3)))
                .thenReturn(Optional.ofNullable(userWrapper.getBodyPartByIndexFromSingleExercise(3)));
        when(bodyPartRepository.findById(bodyPartWrapper.getSingleId()))
                .thenReturn(Optional.of(bodyPartWrapper.getSingle()));

        when(httpRefRepository.findById(userWrapper.getHttpRefIdByIndexFromSingleExercise(0)))
                .thenReturn(Optional.of(userWrapper.getHttpRefByIndexFromSingleExercise(0)));
        when(httpRefRepository.findById(userWrapper.getHttpRefIdByIndexFromSingleExercise(3)))
                .thenReturn(Optional.of(userWrapper.getHttpRefByIndexFromSingleExercise(3)));
        when(httpRefRepository.findById(mediaCustomWrapper.getSingleId()))
                .thenReturn(Optional.of(mediaCustomWrapper.getSingle()));
        when(httpRefRepository.findById(mediaDefaultWrapper.getSingle().getId()))
                .thenReturn(Optional.of(mediaDefaultWrapper.getSingle()));

        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        ExerciseResponseDto responseDto = exerciseService.updateCustomExercise(
                userWrapper.getExerciseIdSingle(), userWrapper.getUserId(), requestDtoWrapper.getDto());

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdSingle(), userWrapper.getUserId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());

        if (nonNull(updateTitle)) {
            verify(exerciseRepository, times(1))
                    .findCustomByTitleAndUserId(requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId());
        }
        verify(bodyPartRepository, times(3)).findById(anyLong());
        verify(httpRefRepository, times(4)).findById(anyLong());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));

        assertEquals(responseDto.getId(), userWrapper.getExerciseIdSingle());
        assertTrue(responseDto.isCustom());

        if (nonNull(updateTitle)) assertEquals(requestDtoWrapper.getFieldValue("title"), responseDto.getTitle());
        else assertEquals(initialTitle, responseDto.getTitle());

        if (nonNull(updateDescription))
            assertEquals(requestDtoWrapper.getFieldValue("description"), responseDto.getDescription());
        else assertEquals(initialDescription, responseDto.getDescription());

        if (nonNull(updateNeedsEquipment))
            assertEquals(requestDtoWrapper.getFieldValue("needsEquipment"), responseDto.isNeedsEquipment());
        else assertEquals(initialNeedsEquipment, responseDto.isNeedsEquipment());

        TestUtilities.assertBodyPartsResponseDtoList(responseDto.getBodyParts(), expectedBodyParts);
        TestUtilities.assertHttpRefsResponseDtoList(responseDto.getHttpRefs(), expectedHttpRefs);
    }

    static Stream<Arguments> updateCustomExerciseMultipleValidInputs() {
        return Stream.of(
                Arguments.of("Update title", "Update description", false),
                Arguments.of("Update title", "Update description", null),
                Arguments.of("Update title", null, false),
                Arguments.of(null, "Update description", false),
                Arguments.of(null, "Update description", null),
                Arguments.of(null, null, false));
    }

    @Test
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDto_whenEmptyHttpRefsIdsListGiven()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(2)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        // Update DTO, medias should be removed from the target exercise. Other fields should remain the same.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);

        List<Long> newHttpRefsIds = Collections.emptyList();

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getExerciseIdSingle()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(userWrapper.getBodyPartsIdsSortedFromSingleExercise())
                .setMediasIds(newHttpRefsIds)
                .buildExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("title", null);
        requestDtoWrapper.setFieldValue("description", null);
        requestDtoWrapper.setFieldValue("needsEquipment", null);

        // Expected nested objects
        List<BodyPart> expectedBodyParts = userWrapper.getBodyPartsSortedFromSingleExercise();
        List<HttpRef> expectedMedias = Collections.emptyList();

        // Mocking
        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId()))
                .thenReturn(Optional.of(userWrapper.getExerciseSingle()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        ExerciseResponseDto responseDto = exerciseService.updateCustomExercise(
                userWrapper.getExerciseIdSingle(), userWrapper.getUserId(), requestDtoWrapper.getDto());

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseSingle().getId(), userWrapper.getUserId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        verify(exerciseRepository, times(0))
                .findCustomByTitleAndUserId(requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));

        assertEquals(responseDto.getId(), userWrapper.getExerciseIdSingle());
        assertTrue(responseDto.isCustom());
        assertEquals(userWrapper.getExerciseSingle().getTitle(), responseDto.getTitle());
        assertEquals(userWrapper.getExerciseSingle().getDescription(), responseDto.getDescription());
        assertEquals(userWrapper.getExerciseSingle().isNeedsEquipment(), responseDto.isNeedsEquipment());
        TestUtilities.assertBodyPartsResponseDtoList(responseDto.getBodyParts(), expectedBodyParts);
        TestUtilities.assertHttpRefsResponseDtoList(responseDto.getHttpRefs(), expectedMedias);
    }

    @Test
    void updateCustomExerciseTest_shouldThrowEmptyRequestAnd400_whenNoUpdatesRequestGiven()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildEmptyExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("bodyPartIds", userWrapper.getBodyPartsIdsSortedFromSingleExercise());
        requestDtoWrapper.setFieldValue("httpRefIds", userWrapper.getHttpRefsIdsSortedFromSingleExercise());

        // Mocking
        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseSingle()));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdSingle(), userWrapper.getUserId());
        verify(userService, times(0)).getUserById(anyLong());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.NO_UPDATES_REQUEST.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowNotFoundAnd404_whenExerciseNotFound() {
        // Given
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();
        long randomExerciseId = 1L;
        long randomUserId = 2L;
        when(exerciseRepository.findCustomByExerciseIdAndUserId(randomExerciseId, randomUserId))
                .thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(randomExerciseId, randomUserId, requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findCustomByExerciseIdAndUserId(randomExerciseId, randomUserId);
        verify(userService, times(0)).getUserById(anyLong());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowNotFoundAnd404_whenExerciseDoesntBelongToUser() {
        UserTestBuilder.UserWrapper userWrapper1 = userTestBuilder.getWrapper();
        userWrapper1
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        UserTestBuilder.UserWrapper userWrapper2 = userTestBuilder.getWrapper();
        userWrapper2
                .setUserIdOrSeed(2)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(2)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(2)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper1.getExerciseIdSingle(), userWrapper2.getUserId()))
                .thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper1.getExerciseIdSingle(), userWrapper2.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper1.getExerciseIdSingle(), userWrapper2.getUserId());
        verify(userService, times(0)).getUserById(userWrapper2.getUserId());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowTitleDuplicateAnd400_whenExerciseWithNewTitleAlreadyExists()
            throws NoSuchFieldException, IllegalAccessException {
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseTestBuilder.ExerciseWrapper exerciseAlreadyExistsWrapper = exerciseTestBuilder.getWrapper();
        exerciseAlreadyExistsWrapper
                .setIdOrSeed(2)
                .setIsExerciseCustom(true)
                .setNeedsEquipment(false)
                .setIsHttpRefCustom(false)
                .setAmountOfNestedEntities(1)
                .setStartIdOrSeedForNestedEntities(2)
                .buildSingle();
        userWrapper.addCustomExercises(exerciseAlreadyExistsWrapper.getExerciseSingleAsList());

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue(
                "title", exerciseAlreadyExistsWrapper.getExerciseSingle().getTitle());

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdFromSortedList(0), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseFromSortedList(0)));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(exerciseAlreadyExistsWrapper.getExerciseSingle()));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getExerciseIdFromSortedList(0),
                        userWrapper.getUserId(),
                        requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdFromSortedList(0), userWrapper.getUserId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        verify(exerciseRepository, times(1))
                .findCustomByTitleAndUserId((String) requestDtoWrapper.getFieldValue("title"), userWrapper.getUserId());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.TITLE_DUPLICATE.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowInvalidNestedObjectAnd400_whenBodyPartNotFound()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        long nonExistingBodyPartId = 100L;

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("bodyPartIds", List.of(nonExistingBodyPartId));

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseSingle()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper.getUserId()))
                .thenReturn(Optional.empty());
        when(bodyPartRepository.findById(nonExistingBodyPartId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdSingle(), userWrapper.getUserId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        verify(exerciseRepository, times(1)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(1)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.INVALID_NESTED_OBJECT.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowUserResourceMismatchAnd400_whenNewMediaDoesntBelongToUser()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper1 = userTestBuilder.getWrapper();
        userWrapper1
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(true)
                .buildUserAndAddSingleExercise();

        UserTestBuilder.UserWrapper userWrapper2 = userTestBuilder.getWrapper();
        userWrapper2
                .setUserIdOrSeed(2)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(2)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(2)
                .setIsExerciseHttpRefsCustom(true)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("httpRefIds", userWrapper2.getHttpRefsIdsSortedFromSingleExercise());
        requestDtoWrapper.setFieldValue("bodyPartIds", userWrapper1.getBodyPartsIdsSortedFromSingleExercise());

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper1.getExerciseIdSingle(), userWrapper1.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper1.getExerciseSingle()));
        when(userService.getUserById(userWrapper1.getUserId())).thenReturn(userWrapper1.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper1.getUserId()))
                .thenReturn(Optional.empty());
        when(httpRefRepository.findById(
                        requestDtoWrapper.getDto().getHttpRefIds().get(0)))
                .thenReturn(Optional.ofNullable(userWrapper2.getHttpRefByIndexFromSingleExercise(0)));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper1.getExerciseIdSingle(), userWrapper1.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper1.getExerciseIdSingle(), userWrapper1.getUserId());
        verify(userService, times(1)).getUserById(userWrapper1.getUserId());
        verify(exerciseRepository, times(1)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(1)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultipleValidButNotDifferentInputs")
    void updateCustomExerciseTest_shouldThrowFieldIsNotDifferentAnd400_whenNewFieldValueIsNotDifferent(
            String updateTitle, String updateDescription, Boolean updateNeedsEquipment)
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setIsExerciseCustom(true)
                .setExerciseIdOrSeed(1)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildEmptyExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("bodyPartIds", userWrapper.getBodyPartsIdsSortedFromSingleExercise());

        if (nonNull(updateTitle)) requestDtoWrapper.setFieldValue("title", updateTitle);
        if (nonNull(updateDescription)) requestDtoWrapper.setFieldValue("description", updateDescription);
        if (nonNull(updateNeedsEquipment)) requestDtoWrapper.setFieldValue("needsEquipment", updateNeedsEquipment);

        when(exerciseRepository.findCustomByExerciseIdAndUserId(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseSingle()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getExerciseIdSingle(), userWrapper.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1))
                .findCustomByExerciseIdAndUserId(userWrapper.getExerciseIdSingle(), userWrapper.getUserId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());

        if (nonNull(updateTitle)) assertEquals(ErrorMessage.TITLES_ARE_NOT_DIFFERENT.getName(), exception.getMessage());
        if (nonNull(updateDescription))
            assertEquals(ErrorMessage.DESCRIPTIONS_ARE_NOT_DIFFERENT.getName(), exception.getMessage());
        if (nonNull(updateNeedsEquipment))
            assertEquals(ErrorMessage.NEEDS_EQUIPMENT_ARE_NOT_DIFFERENT.getName(), exception.getMessage());
    }

    static Stream<Arguments> updateCustomExerciseMultipleValidButNotDifferentInputs() {
        return Stream.of(
                Arguments.of("Title 1", null, null),
                Arguments.of(null, "Description 1", null),
                Arguments.of(null, null, true));
    }
}
