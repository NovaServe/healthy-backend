package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
@Import(BeanConfig.class)
class WorkoutControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DbUtil dbUtil;

    @Autowired
    TestUtil testUtil;

    @Autowired
    DtoUtil dtoUtil;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomWorkoutTest_shouldReturnWorkoutResponseDtoWith201_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);

        WorkoutCreateRequestDto requestDto =
                dtoUtil.workoutCreateRequestDto(1, List.of(defaultExercise.getId(), customExercise.getId()));

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(requestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andExpect(jsonPath("$.needsEquipment", is(needsEquipment)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        // Db
        Workout createdWorkout = dbUtil.getWorkoutById(responseDto.getId());
        assertEquals(user.getId(), createdWorkout.getUser().getId());
        assertWorkout(responseDto, createdWorkout);
        assertWorkout(responseDto, List.of(bodyPart1, bodyPart2), List.of(defaultExercise, customExercise));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomWorkoutTest_shouldReturnErrorMessageWith400_whenWorkoutAlreadyExistsWithSameTitle()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        WorkoutCreateRequestDto requestDto =
                dtoUtil.workoutCreateRequestDto(1, List.of(defaultExercise.getId(), customExercise.getId()));
        requestDto.setTitle(customWorkout.getTitle());

        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomWorkoutTest_shouldReturnErrorMessageWith404_whenExerciseNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long nonExistentExerciseId = 1000L;
        WorkoutCreateRequestDto requestDto = dtoUtil.workoutCreateRequestDto(1, List.of(nonExistentExerciseId));
        ApiException expectedException =
                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistentExerciseId, HttpStatus.NOT_FOUND);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomWorkoutTest_shouldReturnErrorMessageWith400_whenExerciseDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user2);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user2);

        WorkoutCreateRequestDto requestDto =
                dtoUtil.workoutCreateRequestDto(1, List.of(defaultExercise.getId(), customExercise.getId()));

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, customExercise.getId(), HttpStatus.BAD_REQUEST);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_WORKOUTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnDefaultWorkoutDtoWith200_whenIdIsValid() throws Exception {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(2, needsEquipment, List.of(bodyPart2), List.of(defaultHttpRef2));
        Workout defaultWorkout = dbUtil.createDefaultWorkout(1, List.of(defaultExercise1, defaultExercise2));

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.DEFAULT_WORKOUT_ID, defaultWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertWorkout(responseDto, defaultWorkout);
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnErrorMessageWith404_whenWorkoutNotFound() throws Exception {
        // Given
        long nonExistentDefaultWorkoutId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistentDefaultWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_WORKOUT_ID, nonExistentDefaultWorkoutId)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnErrorMessageWith400_whenCustomWorkoutRequestedInsteadOfDefault()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        ApiException expectedException = new ApiException(
                ErrorMessage.CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.DEFAULT_WORKOUT_ID, customWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultWorkoutDtoListWith200_whenNoUrlParam() throws Exception {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(2, needsEquipment, List.of(bodyPart2), List.of(defaultHttpRef2));
        Workout defaultWorkout1 = dbUtil.createDefaultWorkout(1, List.of(defaultExercise1, defaultExercise2));

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef4 = dbUtil.createDefaultHttpRef(4);
        needsEquipment = false;
        Exercise defaultExercise3 =
                dbUtil.createDefaultExercise(3, needsEquipment, List.of(bodyPart3), List.of(defaultHttpRef3));
        Exercise defaultExercise4 =
                dbUtil.createDefaultExercise(4, needsEquipment, List.of(bodyPart4), List.of(defaultHttpRef4));
        Workout defaultWorkout2 = dbUtil.createDefaultWorkout(2, List.of(defaultExercise3, defaultExercise4));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_WORKOUTS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<WorkoutResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<WorkoutResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertWorkout(responseDto.get(0), defaultWorkout1);
        assertWorkout(responseDto.get(1), defaultWorkout2);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomWorkoutByIdTest_shouldReturnCustomWorkoutDtoWith200_whenIdIsValid() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = false;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.CUSTOM_WORKOUT_ID, customWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertWorkout(responseDto, customWorkout);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomWorkoutByIdTest_shouldReturnErrorMessageWith400_whenDefaultWorkoutRequestedInsteadOfCustom()
            throws Exception {
        // Given
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        boolean needsEquipment = true;
        Exercise defaultExercise1 =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 =
                dbUtil.createDefaultExercise(2, needsEquipment, List.of(bodyPart2), List.of(defaultHttpRef2));
        Workout defaultWorkout = dbUtil.createDefaultWorkout(1, List.of(defaultExercise1, defaultExercise2));

        ApiException expectedException = new ApiException(
                ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_WORKOUT_ID, defaultWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnWorkoutResponseDtoWith200_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        BodyPart bodyPart3 = dbUtil.createBodyPart(3);
        BodyPart bodyPart4 = dbUtil.createBodyPart(4);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);
        Exercise defaultExerciseToAdd =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart3), List.of(defaultHttpRef1));
        Exercise customExerciseToAdd =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart4), List.of(customHttpRef1), user);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(
                1, List.of(defaultExercise.getId(), defaultExerciseToAdd.getId(), customExerciseToAdd.getId()));

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(requestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andExpect(jsonPath("$.needsEquipment", is(needsEquipment)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        Workout updatedWorkout = dbUtil.getWorkoutById(responseDto.getId());
        assertWorkout(responseDto, updatedWorkout);
        assertWorkout(
                responseDto,
                List.of(bodyPart1, bodyPart3, bodyPart4),
                List.of(defaultExercise, defaultExerciseToAdd, customExerciseToAdd));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith400_whenEmptyRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDtoEmpty();
        ApiException expectedException = new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith404_whenWorkoutNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart), List.of(defaultHttpRef));
        long nonExistentWorkoutId = 1000L;

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(1, List.of(defaultExercise.getId()));
        ApiException expectedException =
                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistentWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, nonExistentWorkoutId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith400_whenWorkoutDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user2);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user2);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user2);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(2, List.of(customExercise.getId()));
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, customWorkout.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith400_whenWorkoutTitleDuplicated() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout alreayExistentCustomWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise), user);
        Workout customWorkoutToUpdate = dbUtil.createCustomWorkout(2, List.of(customExercise), user);

        WorkoutUpdateRequestDto requestDto =
                dtoUtil.workoutUpdateRequestDto(3, List.of(defaultExercise.getId(), customExercise.getId()));
        requestDto.setTitle(alreayExistentCustomWorkout.getTitle());

        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkoutToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith404_whenExerciseNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        long nonExistentExerciseId = 1000L;

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(2, List.of(nonExistentExerciseId));
        ApiException expectedException =
                new ApiException(ErrorMessage.EXERCISE_NOT_FOUND, nonExistentExerciseId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnErrorMessageWith400_whenExerciseDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user = dbUtil.createUser(1, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef1), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(3);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);
        Exercise customExercise2 = dbUtil.createCustomExercise(
                3, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef2, customHttpRef2), user2);

        WorkoutUpdateRequestDto requestDto = dtoUtil.workoutUpdateRequestDto(2, List.of(customExercise2.getId()));
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_EXERCISE_MISMATCH, customExercise2.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_WORKOUT_ID, customWorkout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomWorkoutTest_shouldReturnVoidWith204_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user);

        long bodyPartId1 = bodyPart1.getId();
        long bodyPartId2 = bodyPart2.getId();
        long defaultHttpRefId = defaultHttpRef.getId();
        long customHttpRefId = customHttpRef.getId();
        long defaultExerciseId = defaultExercise.getId();
        long customExerciseId = customExercise.getId();
        long customWorkoutId = customWorkout.getId();

        // When
        mockMvc.perform(delete(URL.CUSTOM_WORKOUT_ID, customWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        assertNull(dbUtil.getWorkoutById(customWorkoutId));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId1));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId2));
        assertNotNull(dbUtil.getHttpRefById(defaultHttpRefId));
        assertNotNull(dbUtil.getHttpRefById(customHttpRefId));
        assertNotNull(dbUtil.getExerciseById(defaultExerciseId));
        assertNotNull(dbUtil.getExerciseById(customExerciseId));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomWorkoutTest_shouldReturnErrorMessageWith400_whenDefaultWorkoutRequestedInsteadOfCustom()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Workout defaultWorkout = dbUtil.createDefaultWorkout(1, List.of(defaultExercise));

        long bodyPartId1 = bodyPart1.getId();
        long bodyPartId2 = bodyPart2.getId();
        long defaultHttpRefId = defaultHttpRef.getId();
        long defaultExerciseId = defaultExercise.getId();
        long defaultWorkoutId = defaultWorkout.getId();

        ApiException expectedException = new ApiException(
                ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(delete(URL.CUSTOM_WORKOUT_ID, defaultWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());

        assertNotNull(dbUtil.getWorkoutById(defaultWorkoutId));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId1));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId2));
        assertNotNull(dbUtil.getHttpRefById(defaultHttpRefId));
        assertNotNull(dbUtil.getExerciseById(defaultExerciseId));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomWorkoutTest_shouldReturnErrorMessageWith404_whenWorkoutNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long nonExistentWorkoutId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, nonExistentWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(delete(URL.CUSTOM_WORKOUT_ID, nonExistentWorkoutId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomWorkoutTest_shouldReturnErrorMessageWith400_whenWorkoutDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(2, user2);
        boolean needsEquipment = true;
        Exercise defaultExercise =
                dbUtil.createDefaultExercise(1, needsEquipment, List.of(bodyPart1), List.of(defaultHttpRef));
        Exercise customExercise =
                dbUtil.createCustomExercise(2, needsEquipment, List.of(bodyPart2), List.of(customHttpRef), user2);
        Workout customWorkout = dbUtil.createCustomWorkout(1, List.of(defaultExercise, customExercise), user2);

        long bodyPartId1 = bodyPart1.getId();
        long bodyPartId2 = bodyPart2.getId();
        long defaultHttpRefId = defaultHttpRef.getId();
        long customHttpRefId = customHttpRef.getId();
        long defaultExerciseId = defaultExercise.getId();
        long customExerciseId = customExercise.getId();
        long customWorkoutId = customWorkout.getId();

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_WORKOUT_MISMATCH, customWorkout.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(delete(URL.CUSTOM_WORKOUT_ID, customWorkout.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());

        assertNotNull(dbUtil.getWorkoutById(customWorkoutId));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId1));
        assertNotNull(dbUtil.getBodyPartById(bodyPartId2));
        assertNotNull(dbUtil.getHttpRefById(defaultHttpRefId));
        assertNotNull(dbUtil.getHttpRefById(customHttpRefId));
        assertNotNull(dbUtil.getExerciseById(defaultExerciseId));
        assertNotNull(dbUtil.getExerciseById(customExerciseId));
    }

    private void assertWorkout(WorkoutResponseDto workoutResponseDto, Workout workout) {
        assertThat(workoutResponseDto)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "user", "needsEquipment")
                .isEqualTo(workout);

        List<BodyPartResponseDto> workoutResponseDto_distinctBodyParts = workoutResponseDto.getBodyParts();
        assertThat(workoutResponseDto_distinctBodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(workout.getDistinctBodyPartsSortedById());

        assertThat(workoutResponseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs")
                .isEqualTo(workout.getExercisesSortedById());

        for (int i = 0; i < workoutResponseDto.getExercises().size(); i++) {
            ExerciseResponseDto exerciseResponseDto =
                    workoutResponseDto.getExercises().get(i);

            assertThat(exerciseResponseDto.getBodyParts())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(workout.getExercisesSortedById().get(i).getBodyPartsSortedById());

            assertThat(exerciseResponseDto.getHttpRefs())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(workout.getExercisesSortedById().get(i).getHttpRefsSortedById());
        }
    }

    private void assertWorkout(
            WorkoutResponseDto workoutResponseDto,
            List<BodyPart> workoutDistinctBodyParts,
            List<Exercise> workoutExercises) {
        List<BodyPartResponseDto> workoutResponseDto_distinctBodyParts = workoutResponseDto.getBodyParts();
        assertThat(workoutResponseDto_distinctBodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(workoutDistinctBodyParts);

        assertThat(workoutResponseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs")
                .isEqualTo(workoutExercises);

        for (int i = 0; i < workoutResponseDto.getExercises().size(); i++) {
            ExerciseResponseDto exerciseResponseDto =
                    workoutResponseDto.getExercises().get(i);

            assertThat(exerciseResponseDto.getBodyParts())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(workoutExercises.get(i).getBodyPartsSortedById());

            assertThat(exerciseResponseDto.getHttpRefs())
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(workoutExercises.get(i).getHttpRefsSortedById());
        }
    }
}
