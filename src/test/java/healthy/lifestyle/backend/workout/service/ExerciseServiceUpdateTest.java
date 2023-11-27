package healthy.lifestyle.backend.workout.service;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import healthy.lifestyle.backend.data.*;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.User;
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

    DataUtil dataUtil = new DataUtil();

    UserTestBuilder userTestBuilder = new UserTestBuilder();

    BodyPartTestBuilder bodyPartTestBuilder = new BodyPartTestBuilder();

    MediaTestBuilder mediaTestBuilder = new MediaTestBuilder();

    ExerciseDtoTestBuilder exerciseDtoTestBuilder = new ExerciseDtoTestBuilder();

    ExerciseTestBuilder exerciseTestBuilder = new ExerciseTestBuilder();

    @Test
    void updateCustomExerciseTest_shouldReturnExerciseDto_whenValidRequestDtoProvidedOld() {
        // Given
        List<BodyPart> bodyParts = dataUtil.createBodyParts(1, 4);
        List<HttpRef> httpRefsCustom = dataUtil.createHttpRefs(1, 4, true);
        List<HttpRef> httpRefsDefault = dataUtil.createHttpRefs(5, 6, false);
        Exercise exercise = dataUtil.createExercise(
                1,
                true,
                true,
                new HashSet<>() {
                    {
                        add(bodyParts.get(0));
                        add(bodyParts.get(1));
                    }
                },
                new HashSet<>() {
                    {
                        add(httpRefsCustom.get(0));
                        add(httpRefsCustom.get(1));
                    }
                });

        User user = dataUtil.createUserEntity(1);
        user.setHttpRefs(new HashSet<>() {
            {
                add(httpRefsCustom.get(0));
                add(httpRefsCustom.get(1));
                add(httpRefsCustom.get(2));
                add(httpRefsCustom.get(3));
            }
        });
        user.setExercises(Set.of(exercise));

        ExerciseUpdateRequestDto requestDto = dataUtil.exerciseUpdateRequestDto(
                1,
                false,
                List.of(bodyParts.get(1).getId(), bodyParts.get(2).getId()),
                List.of(
                        httpRefsCustom.get(1).getId(),
                        httpRefsCustom.get(2).getId(),
                        httpRefsDefault.get(0).getId()));

        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(exerciseRepository.findCustomByTitleAndUserId(requestDto.getTitle(), user.getId()))
                .thenReturn(Optional.empty());

        when(bodyPartRepository.findById(bodyParts.get(0).getId())).thenReturn(Optional.of(bodyParts.get(0)));
        when(bodyPartRepository.findById(bodyParts.get(2).getId())).thenReturn(Optional.of(bodyParts.get(2)));

        when(httpRefRepository.findById(httpRefsCustom.get(0).getId())).thenReturn(Optional.of(httpRefsCustom.get(0)));
        when(httpRefRepository.findById(httpRefsCustom.get(2).getId())).thenReturn(Optional.of(httpRefsCustom.get(2)));
        when(httpRefRepository.findById(httpRefsDefault.get(0).getId()))
                .thenReturn(Optional.of(httpRefsDefault.get(0)));

        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        ExerciseResponseDto responseDto =
                exerciseService.updateCustomExercise(exercise.getId(), user.getId(), requestDto);

        // Then
        verify(exerciseRepository, times(1)).findById(exercise.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(exerciseRepository, times(1)).findCustomByTitleAndUserId(requestDto.getTitle(), user.getId());
        verify(bodyPartRepository, times(2)).findById(anyLong());
        verify(httpRefRepository, times(3)).findById(anyLong());

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("bodyParts", "httpRefs", "id", "isCustom")
                .isEqualTo(requestDto);

        assertEquals(responseDto.getId(), exercise.getId());
        assertTrue(responseDto.isCustom());

        assertThat(List.of(bodyParts.get(1), bodyParts.get(2)))
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        assertThat(List.of(httpRefsCustom.get(1), httpRefsCustom.get(2), httpRefsDefault.get(0)))
                .usingRecursiveComparison()
                .ignoringFields("exercises", "user")
                .isEqualTo(responseDto.getHttpRefs());
    }

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultiplePositiveInputs")
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDto_whenValidRequestDtoProvided(
            String updateTitle, String updateDescription, Boolean updateNeedsEquipment)
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        // User with one exercise and related body parts and default media.
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        // Custom and default medias to update existing exercise.
        MediaTestBuilder.MediaWrapper mediaCustomWrapper = mediaTestBuilder.getWrapper();
        mediaCustomWrapper
                .setMediaCustom(true)
                .setId(10)
                .setUser(userWrapper.getUser())
                .buildSingleMedia();
        userWrapper.addCustomMedias(mediaCustomWrapper.getEntityCollection());

        MediaTestBuilder.MediaWrapper mediaDefaultWrapper = mediaTestBuilder.getWrapper();
        mediaDefaultWrapper.setMediaCustom(false).setId(20).buildSingleMedia();

        BodyPartTestBuilder.BodyPartWrapper bodyPartWrapper = bodyPartTestBuilder.getWrapper();
        bodyPartWrapper.setId(10).buildSingleBodyPart();

        // Update DTO, nested entities should be added and removed as well.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();

        List<Long> newBodyPartsIds = List.of(
                userWrapper.getBodyPartIdFromSingleExercise(1),
                userWrapper.getBodyPartIdFromSingleExercise(2),
                bodyPartWrapper.getSingleId());
        List<Long> newMediasIds = List.of(
                userWrapper.getMediaIdFromSingleExercise(1),
                userWrapper.getMediaIdFromSingleExercise(2),
                mediaCustomWrapper.getSingleId(),
                mediaDefaultWrapper.getSingleId());

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getSingleExerciseId()))
                .setNeedsEquipment(nonNull(updateNeedsEquipment) ? updateNeedsEquipment : true)
                .setBodyPartsIds(newBodyPartsIds)
                .setMediasIds(newMediasIds)
                .buildUpdateExerciseDto();
        if (nonNull(updateTitle)) requestDtoWrapper.setFieldValue("title", updateTitle);
        if (nonNull(updateDescription)) requestDtoWrapper.setFieldValue("description", updateDescription);

        // Expected nested objects
        List<BodyPart> expectedBodyParts = List.of(
                userWrapper.getBodyPartFromSingleExercise(1),
                userWrapper.getBodyPartFromSingleExercise(2),
                bodyPartWrapper.getSingle());
        List<HttpRef> expectedMedias = List.of(
                userWrapper.getMediaFromSingleExercise(1),
                userWrapper.getMediaFromSingleExercise(2),
                mediaCustomWrapper.getSingle(),
                mediaDefaultWrapper.getSingle());

        // Mocking
        when(exerciseRepository.findById(userWrapper.getSingleExerciseId()))
                .thenReturn(Optional.of(userWrapper.getSingleExercise()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        if (nonNull(updateTitle)) {
            when(exerciseRepository.findCustomByTitleAndUserId(
                            requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId()))
                    .thenReturn(Optional.empty());
        }

        when(bodyPartRepository.findById(userWrapper.getBodyPartIdFromSingleExercise(0)))
                .thenReturn(Optional.ofNullable(userWrapper.getBodyPartFromSingleExercise(0)));
        when(bodyPartRepository.findById(userWrapper.getBodyPartIdFromSingleExercise(3)))
                .thenReturn(Optional.ofNullable(userWrapper.getBodyPartFromSingleExercise(3)));
        when(bodyPartRepository.findById(bodyPartWrapper.getSingleId()))
                .thenReturn(Optional.of(bodyPartWrapper.getSingle()));

        when(httpRefRepository.findById(userWrapper.getMediaIdFromSingleExercise(0)))
                .thenReturn(Optional.of(userWrapper.getMediaFromSingleExercise(0)));
        when(httpRefRepository.findById(userWrapper.getMediaIdFromSingleExercise(3)))
                .thenReturn(Optional.of(userWrapper.getMediaFromSingleExercise(3)));
        when(httpRefRepository.findById(mediaCustomWrapper.getSingleId()))
                .thenReturn(Optional.of(mediaCustomWrapper.getSingle()));
        when(httpRefRepository.findById(mediaDefaultWrapper.getSingle().getId()))
                .thenReturn(Optional.of(mediaDefaultWrapper.getSingle()));

        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        ExerciseResponseDto responseDto = exerciseService.updateCustomExercise(
                userWrapper.getSingleExerciseId(), userWrapper.getUserId(), requestDtoWrapper.getDto());

        // Then
        verify(exerciseRepository, times(1))
                .findById(userWrapper.getSingleExercise().getId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        if (nonNull(updateTitle)) {
            verify(exerciseRepository, times(1))
                    .findCustomByTitleAndUserId(requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId());
        }
        verify(bodyPartRepository, times(3)).findById(anyLong());
        verify(httpRefRepository, times(4)).findById(anyLong());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));

        assertEquals(responseDto.getId(), userWrapper.getSingleExerciseId());
        assertTrue(responseDto.isCustom());
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("bodyParts", "httpRefs", "id", "isCustom")
                .isEqualTo(requestDtoWrapper.getDto());
        TestUtilities.assertBodyParts(responseDto.getBodyParts(), expectedBodyParts);
        TestUtilities.assertHttpRefs(responseDto.getHttpRefs(), expectedMedias);
    }

    static Stream<Arguments> updateCustomExerciseMultiplePositiveInputs() {
        return Stream.of(
                Arguments.of("Update title", "Update description", false),
                Arguments.of("Update title", "Update description", null),
                Arguments.of("Update title", null, false),
                Arguments.of(null, "Update description", false),
                Arguments.of(null, "Update description", null),
                Arguments.of(null, null, false));
    }

    @Test
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDto_whenEmptyMediasIdsListProvided()
            throws NoSuchFieldException, IllegalAccessException {
        // Given
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(2)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        // Update DTO, medias should be removed from the target exercise.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();

        List<Long> newMediasIds = Collections.emptyList();

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getSingleExerciseId()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(userWrapper.getBodyPartsIdsFromSingleExercise())
                .setMediasIds(newMediasIds)
                .buildUpdateExerciseDto();
        requestDtoWrapper.setFieldValue("title", userWrapper.getSingleExercise().getTitle());
        requestDtoWrapper.setFieldValue(
                "description", userWrapper.getSingleExercise().getDescription());

        // Expected nested objects
        List<BodyPart> expectedBodyParts = userWrapper.getBodyPartsSortedFromSingleExercise();
        List<HttpRef> expectedMedias = Collections.emptyList();

        // Mocking
        when(exerciseRepository.findById(userWrapper.getSingleExerciseId()))
                .thenReturn(Optional.of(userWrapper.getSingleExercise()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.save(any(Exercise.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        // When
        ExerciseResponseDto responseDto = exerciseService.updateCustomExercise(
                userWrapper.getSingleExerciseId(), userWrapper.getUserId(), requestDtoWrapper.getDto());

        // Then
        verify(exerciseRepository, times(1))
                .findById(userWrapper.getSingleExercise().getId());
        verify(userService, times(1)).getUserById(userWrapper.getUserId());
        verify(exerciseRepository, times(0))
                .findCustomByTitleAndUserId(requestDtoWrapper.getDto().getTitle(), userWrapper.getUserId());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());
        verify(exerciseRepository, times(1)).save(any(Exercise.class));

        assertEquals(responseDto.getId(), userWrapper.getSingleExerciseId());
        assertTrue(responseDto.isCustom());
        assertTrue(responseDto.isNeedsEquipment());
        assertEquals(userWrapper.getSingleExercise().getTitle(), responseDto.getTitle());
        assertEquals(userWrapper.getSingleExercise().getDescription(), responseDto.getDescription());
        TestUtilities.assertBodyParts(responseDto.getBodyParts(), expectedBodyParts);
        TestUtilities.assertHttpRefs(responseDto.getHttpRefs(), expectedMedias);
    }

    @Test
    void updateCustomExerciseTest_shouldThrowEmptyRequestAnd400_whenEmptyRequestDtoProvided() {
        // Given
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildEmptyUpdateExerciseDto();
        long randomExerciseId = 100L;
        long randomUserId = 100L;

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(randomExerciseId, randomUserId, requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(0)).findById(anyLong());
        verify(userService, times(0)).getUserById(anyLong());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.EMPTY_REQUEST.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowNotFoundAnd404_whenExerciseNotFound() {
        // Given
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildRandomUpdateExerciseDto();
        long randomExerciseId = 1L;
        long randomUserId = 2L;
        when(exerciseRepository.findById(randomExerciseId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(randomExerciseId, randomUserId, requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findById(randomExerciseId);
        verify(userService, times(0)).getUserById(anyLong());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.NOT_FOUND.getName(), exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowUserResourceMismatchAnd400_whenExerciseDoesntBelongToUser() {
        UserTestBuilder.UserWrapper userWrapper1 = userTestBuilder.getWrapper();
        userWrapper1
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        UserTestBuilder.UserWrapper userWrapper2 = userTestBuilder.getWrapper();
        userWrapper2
                .setUserId(2)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(2)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(2)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildRandomUpdateExerciseDto();

        when(exerciseRepository.findById(userWrapper1.getSingleExerciseId()))
                .thenReturn(Optional.ofNullable(userWrapper1.getSingleExercise()));
        when(userService.getUserById(userWrapper2.getUserId())).thenReturn(userWrapper2.getUser());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper1.getSingleExerciseId(), userWrapper2.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findById(userWrapper1.getSingleExerciseId());
        verify(userService, times(1)).getUserById(userWrapper2.getUserId());
        verify(exerciseRepository, times(0)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(0)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }

    @Test
    void updateCustomExerciseTest_shouldThrowTitleDuplicateAnd400_whenExerciseWithNewTitleAlreadyExists()
            throws NoSuchFieldException, IllegalAccessException {
        UserTestBuilder.UserWrapper userWrapper = userTestBuilder.getWrapper();
        userWrapper
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseTestBuilder.ExerciseWrapper exerciseAlreadyExistsWrapper = exerciseTestBuilder.getWrapper();
        exerciseAlreadyExistsWrapper
                .setId(2)
                .setExerciseCustom(true)
                .setNeedsEquipment(false)
                .setMediaCustom(false)
                .setAmountOfNestedEntities(1)
                .setStartIdForNestedEntities(2)
                .buildSingle();
        userWrapper.addCustomExercises(exerciseAlreadyExistsWrapper.getSingleExerciseCollection());

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildRandomUpdateExerciseDto();
        requestDtoWrapper.setFieldValue(
                "title", exerciseAlreadyExistsWrapper.getSingleExercise().getTitle());

        when(exerciseRepository.findById(userWrapper.getExerciseIdFromSortedList(0)))
                .thenReturn(Optional.ofNullable(userWrapper.getExerciseFromSortedList(0)));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper.getUserId()))
                .thenReturn(Optional.ofNullable(exerciseAlreadyExistsWrapper.getSingleExercise()));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getExerciseIdFromSortedList(0),
                        userWrapper.getUserId(),
                        requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findById(userWrapper.getExerciseIdFromSortedList(0));
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
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(false)
                .buildUserAndAddSingleExercise();

        long nonExistingBodyPartId = 100L;

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildRandomUpdateExerciseDto();
        requestDtoWrapper.setFieldValue("bodyPartIds", List.of(nonExistingBodyPartId));

        when(exerciseRepository.findById(userWrapper.getSingleExerciseId()))
                .thenReturn(Optional.ofNullable(userWrapper.getSingleExercise()));
        when(userService.getUserById(userWrapper.getUserId())).thenReturn(userWrapper.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper.getUserId()))
                .thenReturn(Optional.empty());
        when(bodyPartRepository.findById(nonExistingBodyPartId)).thenReturn(Optional.empty());

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper.getSingleExerciseId(), userWrapper.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findById(userWrapper.getSingleExerciseId());
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
                .setUserId(1)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(1)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(1)
                .setMediaCustom(true)
                .buildUserAndAddSingleExercise();

        UserTestBuilder.UserWrapper userWrapper2 = userTestBuilder.getWrapper();
        userWrapper2
                .setUserId(2)
                .setRoleUserWithId(1)
                .setExerciseCustom(true)
                .setExerciseId(2)
                .setExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdForExerciseNestedEntities(2)
                .setMediaCustom(true)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper();
        requestDtoWrapper.buildRandomUpdateExerciseDto();
        requestDtoWrapper.setFieldValue("httpRefIds", List.of(userWrapper2.getMediaIdFromSingleExercise(0)));
        requestDtoWrapper.setFieldValue("bodyPartIds", List.of(userWrapper1.getBodyPartIdFromSingleExercise(0)));

        when(exerciseRepository.findById(userWrapper1.getSingleExerciseId()))
                .thenReturn(Optional.ofNullable(userWrapper1.getSingleExercise()));
        when(userService.getUserById(userWrapper1.getUserId())).thenReturn(userWrapper1.getUser());
        when(exerciseRepository.findCustomByTitleAndUserId(
                        (String) requestDtoWrapper.getFieldValue("title"), userWrapper1.getUserId()))
                .thenReturn(Optional.empty());
        when(httpRefRepository.findById(
                        requestDtoWrapper.getDto().getHttpRefIds().get(0)))
                .thenReturn(Optional.ofNullable(userWrapper2.getMediaFromSingleExercise(0)));

        // When
        ApiException exception = assertThrows(
                ApiException.class,
                () -> exerciseService.updateCustomExercise(
                        userWrapper1.getSingleExerciseId(), userWrapper1.getUserId(), requestDtoWrapper.getDto()));

        // Then
        verify(exerciseRepository, times(1)).findById(userWrapper1.getSingleExerciseId());
        verify(userService, times(1)).getUserById(userWrapper1.getUserId());
        verify(exerciseRepository, times(1)).findCustomByTitleAndUserId(anyString(), anyLong());
        verify(bodyPartRepository, times(0)).findById(anyLong());
        verify(httpRefRepository, times(1)).findById(anyLong());

        assertEquals(ErrorMessage.USER_RESOURCE_MISMATCH.getName(), exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getHttpStatus().value());
    }
}
