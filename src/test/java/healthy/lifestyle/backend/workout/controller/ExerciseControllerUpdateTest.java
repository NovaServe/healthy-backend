package healthy.lifestyle.backend.workout.controller;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.*;
import healthy.lifestyle.backend.data.bodypart.BodyPartJpaTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseDtoTestBuilder;
import healthy.lifestyle.backend.data.httpref.HttpRefJpaTestBuilder;
import healthy.lifestyle.backend.data.user.UserJpaTestBuilder;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import({DataConfiguration.class})
public class ExerciseControllerUpdateTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    DataUtil dataUtil;

    @Autowired
    UserJpaTestBuilder userJpaTestBuilder;

    @Autowired
    BodyPartJpaTestBuilder bodyPartJpaTestBuilder;

    @Autowired
    HttpRefJpaTestBuilder mediaJpaTestBuilder;

    @Autowired
    ExerciseDtoTestBuilder exerciseDtoTestBuilder;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String REQUEST_URL = "/api/v1/workouts/exercises/{exerciseId}";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultipleValidInputs")
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDtoAnd200_whenValidDtoProvided(
            String updateTitle, String updateDescription, Boolean updateNeedsEquipment) throws Exception {
        // Given
        // User with one exercise and related body parts and default media.
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        String initialTitle = userWrapper.getExerciseSingle().getTitle();
        String initialDescription = userWrapper.getExerciseSingle().getDescription();
        boolean initialNeedsEquipment = userWrapper.getExerciseSingle().isNeedsEquipment();

        // Custom and default medias to update existing exercise.
        HttpRefJpaTestBuilder.HttpRefWrapper mediaCustomWrapper = mediaJpaTestBuilder.getWrapper();
        mediaCustomWrapper
                .setIsCustom(true)
                .setIdOrSeed(10)
                .setUser(userWrapper.getUser())
                .buildSingle();
        userWrapper.addCustomHttpRefs(mediaCustomWrapper.getSingleAsList());

        HttpRefJpaTestBuilder.HttpRefWrapper mediaDefaultWrapper = mediaJpaTestBuilder.getWrapper();
        mediaDefaultWrapper.setIsCustom(false).setIdOrSeed(20).buildSingle();

        BodyPartJpaTestBuilder.BodyPartWrapper bodyPartWrapper = bodyPartJpaTestBuilder.getWrapper();
        bodyPartWrapper.setIdOrSeed(10).buildSingle();

        // Update DTO, nested entities should be added and removed as well.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);

        List<Long> newBodyPartsIds = List.of(
                userWrapper.getBodyPartIdByIndexFromSingleExercise(1),
                userWrapper.getBodyPartIdByIndexFromSingleExercise(2),
                bodyPartWrapper.getSingleId());
        List<Long> newMediasIds = List.of(
                userWrapper.getHttpRefIdByIndexFromSingleExercise(1),
                userWrapper.getHttpRefIdByIndexFromSingleExercise(2),
                mediaCustomWrapper.getSingleId(),
                mediaDefaultWrapper.getSingleId());

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getExerciseIdSingle()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(newBodyPartsIds)
                .setMediasIds(newMediasIds)
                .buildExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("title", updateTitle);
        requestDtoWrapper.setFieldValue("description", updateDescription);
        requestDtoWrapper.setFieldValue("needsEquipment", updateNeedsEquipment);

        // Expected nested objects
        List<BodyPart> expectedBodyParts = List.of(
                userWrapper.getBodyPartByIndexFromSingleExercise(1),
                userWrapper.getBodyPartByIndexFromSingleExercise(2),
                bodyPartWrapper.getSingle());
        List<HttpRef> expectedMedias = List.of(
                userWrapper.getHttpRefByIndexFromSingleExercise(1),
                userWrapper.getHttpRefByIndexFromSingleExercise(2),
                mediaCustomWrapper.getSingle(),
                mediaDefaultWrapper.getSingle());

        // When
        MvcResult mvcResult = mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto responseDto = objectMapper.readValue(responseContent, ExerciseResponseDto.class);

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
        TestUtilities.assertHttpRefsResponseDtoList(responseDto.getHttpRefs(), expectedMedias);
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
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnUpdatedExerciseDtoAnd200_whenEmptyHttpRefsIdsListProvided()
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        // Update DTO, medias should be removed from the target exercise. Other fields should remain the same.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);

        List<Long> newMediasIds = Collections.emptyList();

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getExerciseIdSingle()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(userWrapper.getBodyPartsIdsSortedFromSingleExercise())
                .setMediasIds(newMediasIds)
                .buildExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("title", null);
        requestDtoWrapper.setFieldValue("description", null);
        requestDtoWrapper.setFieldValue("needsEquipment", null);

        // Expected nested objects
        List<BodyPart> expectedBodyParts = userWrapper.getBodyPartsSortedFromSingleExercise();
        List<HttpRef> expectedMedias = Collections.emptyList();

        // When
        MvcResult mvcResult = mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto responseDto = objectMapper.readValue(responseContent, ExerciseResponseDto.class);

        assertTrue(responseDto.isCustom());
        assertEquals(userWrapper.getExerciseSingle().getTitle(), responseDto.getTitle());
        assertEquals(userWrapper.getExerciseSingle().getDescription(), responseDto.getDescription());
        assertEquals(userWrapper.getExerciseSingle().isNeedsEquipment(), responseDto.isNeedsEquipment());
        TestUtilities.assertBodyPartsResponseDtoList(responseDto.getBodyParts(), expectedBodyParts);
        TestUtilities.assertHttpRefsResponseDtoList(responseDto.getHttpRefs(), expectedMedias);
    }

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultipleInvalidInputs")
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnValidationMessageAnd400_whenInvalidFieldTryToUpdate(
            String updateTitle,
            String updateDescription,
            Boolean updateNeedsEquipment,
            List<Long> updateBodyPartsIds,
            List<Long> updateMediasIds,
            String errorFieldName,
            String errorMessage)
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        // Update DTO, medias should be removed from the target exercise. Other fields should remain the same.
        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);

        List<Long> newMediasIds = Collections.emptyList();

        requestDtoWrapper
                .setSeed(String.valueOf(userWrapper.getExerciseIdSingle()))
                .setNeedsEquipment(true)
                .setBodyPartsIds(userWrapper.getBodyPartsIdsSortedFromSingleExercise())
                .setMediasIds(newMediasIds)
                .buildExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("title", updateTitle);
        requestDtoWrapper.setFieldValue("description", updateDescription);
        requestDtoWrapper.setFieldValue("needsEquipment", updateNeedsEquipment);
        requestDtoWrapper.setFieldValue("bodyPartIds", updateBodyPartsIds);
        requestDtoWrapper.setFieldValue("httpRefIds", updateMediasIds);

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> updateCustomExerciseMultipleInvalidInputs() {
        // String updateTitle, String updateDescription, Boolean updateNeedsEquipment,
        // List<Long> updateBodyPartsIds, List<Long> updateMediasIds,
        // String errorFieldName, String errorMessage
        return Stream.of(
                // Invalid title
                Arguments.of(
                        "Update title$%",
                        null, null, List.of(1L), Collections.emptyList(), "$.title", "Not allowed symbols"),
                // Invalid description
                Arguments.of(
                        null,
                        "Update description#@!",
                        null,
                        List.of(1L),
                        Collections.emptyList(),
                        "$.description",
                        "Not allowed symbols"),
                // Empty list of body part ids
                Arguments.of(
                        null,
                        null,
                        null,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        "$.bodyPartIds",
                        "Should be not empty list"),
                // Null body part ids
                Arguments.of(null, null, null, null, Collections.emptyList(), "$.bodyPartIds", "must not be null"),
                // Null http ref ids
                Arguments.of(null, null, null, List.of(1L), null, "$.httpRefIds", "must not be null"));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenNoUpdatesRequestDtoProvided() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
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

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NO_UPDATES_REQUEST.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd404_whenExerciseNotFound() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();

        long nonExistingExerciseId = 1000L;

        // When
        mockMvc.perform(patch(REQUEST_URL, nonExistingExerciseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd404_whenExerciseDoesntBelongToUser() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper1 = userJpaTestBuilder.getWrapper();
        userWrapper1
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        UserJpaTestBuilder.UserWrapper userWrapper2 = userJpaTestBuilder.getWrapper();
        userWrapper2
                .setUserIdOrSeed(2)
                .setUserRole()
                .setIsRoleAlreadyCreated(true)
                .setExerciseIdOrSeed(2)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(2)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildRandomExerciseUpdateRequestDto();

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper2.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenExerciseWithNewTitleAlreadyExists()
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExercises(2)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddMultipleExercises();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildEmptyExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("bodyPartIds", userWrapper.getBodyPartsIdsSortedFromExerciseListByIndex(0));
        requestDtoWrapper.setFieldValue(
                "title", userWrapper.getExerciseFromSortedList(1).getTitle());

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdFromSortedList(0))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.TITLE_DUPLICATE.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenBodyPartNotFound() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildEmptyExerciseUpdateRequestDto();
        long nonExistingBodyPartId = 1000L;
        requestDtoWrapper.setFieldValue("bodyPartIds", List.of(nonExistingBodyPartId));

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.INVALID_NESTED_OBJECT.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldReturnErrorMessageAnd400_whenHttpRefDoesntBelongToUser() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper1 = userJpaTestBuilder.getWrapper();
        userWrapper1
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(true)
                .buildUserAndAddSingleExercise();

        UserJpaTestBuilder.UserWrapper userWrapper2 = userJpaTestBuilder.getWrapper();
        userWrapper2
                .setUserIdOrSeed(2)
                .setUserRole()
                .setIsRoleAlreadyCreated(true)
                .setExerciseIdOrSeed(2)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExerciseNestedEntities(1)
                .setStartIdOrSeedForExerciseNestedEntities(2)
                .setIsExerciseHttpRefsCustom(true)
                .buildUserAndAddSingleExercise();

        ExerciseDtoTestBuilder.ExerciseDtoWrapper<ExerciseUpdateRequestDto> requestDtoWrapper =
                exerciseDtoTestBuilder.getWrapper(ExerciseUpdateRequestDto.class);
        requestDtoWrapper.buildEmptyExerciseUpdateRequestDto();
        requestDtoWrapper.setFieldValue("httpRefIds", userWrapper2.getHttpRefsIdsSortedFromSingleExercise());
        requestDtoWrapper.setFieldValue("bodyPartIds", userWrapper1.getBodyPartsIdsSortedFromSingleExercise());

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper1.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("updateCustomExerciseMultipleValidButNotDifferentInputs")
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void updateCustomExerciseTest_shouldThrowFieldIsNotDifferentAnd400_whenNewFieldValueIsNotDifferent(
            String updateTitle, String updateDescription, Boolean updateNeedsEquipment) throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
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

        String errorMessage = nonNull(updateTitle)
                ? ErrorMessage.TITLES_ARE_NOT_DIFFERENT.getName()
                : nonNull(updateDescription)
                        ? ErrorMessage.DESCRIPTIONS_ARE_NOT_DIFFERENT.getName()
                        : ErrorMessage.NEEDS_EQUIPMENT_ARE_NOT_DIFFERENT.getName();

        // When
        mockMvc.perform(patch(REQUEST_URL, userWrapper.getExerciseIdSingle())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> updateCustomExerciseMultipleValidButNotDifferentInputs() {
        return Stream.of(
                Arguments.of("Title 1", null, null),
                Arguments.of(null, "Description 1", null),
                Arguments.of(null, null, true));
    }
}
