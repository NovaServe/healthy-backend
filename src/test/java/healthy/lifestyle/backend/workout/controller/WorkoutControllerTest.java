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
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.exception.ExceptionDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.CreateWorkoutRequestDto;
import healthy.lifestyle.backend.workout.dto.UpdateWorkoutRequestDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.*;
import java.util.stream.Stream;
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
@Import(DataConfiguration.class)
class WorkoutControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    DataUtil dataUtil;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String URL = "/api/v1/workouts";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultWorkoutsAnd200Ok_whenNoUrlParam() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        Exercise exercise1 = dataHelper.createExercise(1, false, false, Set.of(bodyPart1), null);
        Exercise exercise2 = dataHelper.createExercise(2, false, false, Set.of(bodyPart1), null);
        Workout workout1 = dataHelper.createWorkout(1, false, Set.of(exercise1, exercise2));

        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        Exercise exercise3 = dataHelper.createExercise(3, false, false, Set.of(bodyPart2), null);
        Exercise exercise4 = dataHelper.createExercise(4, false, true, Set.of(bodyPart3), null);
        Workout workout2 = dataHelper.createWorkout(2, false, Set.of(exercise3, exercise4));

        // When
        String postfix = "/default";
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<WorkoutResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<WorkoutResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertThat(List.of(workout1, workout2))
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "exercises", "bodyParts", "needsEquipment", "users")
                .isEqualTo(responseDto);

        assertFalse(responseDto.get(0).isNeedsEquipment());
        assertTrue(responseDto.get(1).isNeedsEquipment());

        List<BodyPart> workout1_bodyParts = List.of(bodyPart1);
        assertThat(workout1_bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.get(0).getBodyParts());

        List<BodyPart> workout2_bodyParts = List.of(bodyPart2, bodyPart3);
        assertThat(workout2_bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.get(1).getBodyParts());

        List<Exercise> workout1_sortedExercises = workout1.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workout1_sortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.get(0).getExercises());

        List<Exercise> workout2_sortedExercises = workout2.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workout2_sortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.get(1).getExercises());
    }

    @Test
    void getDefaultWorkoutsTest_shouldReturnDefaultWorkoutsAnd200Ok_whenUrlParamProvided() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        Exercise exercise1 = dataHelper.createExercise(1, false, false, Set.of(bodyPart1), null);
        Exercise exercise2 = dataHelper.createExercise(2, false, false, Set.of(bodyPart1), null);
        Workout workout1 = dataHelper.createWorkout(22, false, Set.of(exercise1, exercise2));

        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        Exercise exercise3 = dataHelper.createExercise(3, false, false, Set.of(bodyPart2), null);
        Exercise exercise4 = dataHelper.createExercise(4, false, true, Set.of(bodyPart3), null);
        Workout workout2 = dataHelper.createWorkout(11, false, Set.of(exercise3, exercise4));

        List<Workout> workoutsSortedByTitle = Stream.of(workout1, workout2)
                .sorted(Comparator.comparing(Workout::getTitle))
                .toList();

        // When
        String postfix = "/default?sortFieldName=title";
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<WorkoutResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<WorkoutResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertThat(workoutsSortedByTitle)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "exercises", "bodyParts", "needsEquipment", "users")
                .isEqualTo(responseDto);

        assertTrue(responseDto.get(0).isNeedsEquipment());
        assertFalse(responseDto.get(1).isNeedsEquipment());

        List<BodyPart> workout1_bodyParts = List.of(bodyPart1);
        assertThat(workout1_bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.get(1).getBodyParts());

        List<BodyPart> workout2_bodyParts = List.of(bodyPart2, bodyPart3);
        assertThat(workout2_bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.get(0).getBodyParts());

        List<Exercise> workout1_sortedExercises = workout1.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workout1_sortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.get(1).getExercises());

        List<Exercise> workout2_sortedExercises = workout2.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workout2_sortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.get(0).getExercises());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnDefaultWorkoutAnd200Ok_whenIdIsValid() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        Exercise exercise1 = dataHelper.createExercise(1, false, false, Set.of(bodyPart1, bodyPart2), null);
        Exercise exercise2 = dataHelper.createExercise(2, false, false, Set.of(bodyPart1, bodyPart3), null);
        Workout workout1 = dataHelper.createWorkout(1, false, Set.of(exercise1, exercise2));

        BodyPart bodyPart4 = dataHelper.createBodyPart(4);
        Exercise exercise3 = dataHelper.createExercise(3, false, false, Set.of(bodyPart4), null);
        Exercise exercise4 = dataHelper.createExercise(4, false, true, Set.of(bodyPart4), null);
        Workout workout2 = dataHelper.createWorkout(2, false, Set.of(exercise3, exercise4));

        String postfix = String.format("/default/%d", workout1.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertThat(workout1)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "users")
                .isEqualTo(responseDto);

        List<BodyPart> workoutSortedBodyParts = List.of(bodyPart1, bodyPart2, bodyPart3);
        assertThat(workoutSortedBodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        List<Exercise> workoutSortedExercises = workout1.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workoutSortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.getExercises());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnNotFoundAnd404_whenWorkoutDoesNotExist() throws Exception {
        // Given
        long wrongId = 1000;
        String postfix = String.format("/default/%d", wrongId);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExceptionDto responseDto = objectMapper.readValue(responseContent, new TypeReference<ExceptionDto>() {});

        assertEquals(ErrorMessage.NOT_FOUND.getName(), responseDto.getMessage());
        assertEquals(Integer.valueOf(HttpStatus.NOT_FOUND.value()), responseDto.getCode());
    }

    @Test
    void getDefaultWorkoutByIdTest_shouldReturnUnauthorizedForThisResourceAnd401() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        Exercise exercise1 = dataHelper.createExercise(1, true, false, Set.of(bodyPart1), null);
        Exercise exercise2 = dataHelper.createExercise(2, true, false, Set.of(bodyPart1), null);
        Workout workout = dataHelper.createWorkout(1, true, Set.of(exercise1, exercise2));

        String postfix = String.format("/default/%d", workout.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExceptionDto responseDto = objectMapper.readValue(responseContent, new TypeReference<ExceptionDto>() {});

        assertEquals(ErrorMessage.UNAUTHORIZED_FOR_THIS_RESOURCE.getName(), responseDto.getMessage());
        assertEquals(Integer.valueOf(HttpStatus.UNAUTHORIZED.value()), responseDto.getCode());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomWorkoutTest_shouldReturnCustomWorkoutAnd200Ok_whenIdIsValid() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        Exercise exercise1 = dataHelper.createExercise(1, true, false, Set.of(bodyPart1, bodyPart2), null);
        Exercise exercise2 = dataHelper.createExercise(2, true, false, Set.of(bodyPart1, bodyPart3), null);
        Workout workout1 = dataHelper.createWorkout(1, true, Set.of(exercise1, exercise2));

        String postfix = String.format("/%d", workout1.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertThat(workout1)
                .usingRecursiveComparison()
                .ignoringFields("exercises", "bodyParts", "users")
                .isEqualTo(responseDto);

        List<BodyPart> workoutSortedBodyParts = List.of(bodyPart1, bodyPart2, bodyPart3);
        assertThat(workoutSortedBodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        List<Exercise> workoutSortedExercises = workout1.getExercises().stream()
                .sorted(Comparator.comparingLong(Exercise::getId))
                .toList();
        assertThat(workoutSortedExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto.getExercises());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomWorkoutTest_shouldReturnCustomWorkoutRequiredAnd400_whenWorkoutIsDefault() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        Exercise exercise1 = dataHelper.createExercise(1, false, false, Set.of(bodyPart1, bodyPart2), null);
        Exercise exercise2 = dataHelper.createExercise(2, false, false, Set.of(bodyPart1, bodyPart3), null);
        Workout workout1 = dataHelper.createWorkout(1, false, Set.of(exercise1, exercise2));

        String postfix = String.format("/%d", workout1.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExceptionDto responseDto = objectMapper.readValue(responseContent, new TypeReference<ExceptionDto>() {});

        assertEquals(ErrorMessage.CUSTOM_WORKOUT_REQUIRED.getName(), responseDto.getMessage());
        assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), responseDto.getCode());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomWorkoutTest_shouldReturnWorkoutResponseDtoAnd201_whenValidRequestProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        Country country2 = dataHelper.createCountry(2);

        // User 1 - under test
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        Exercise exercise1 = dataHelper.createExercise(1, true, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 =
                dataHelper.createExercise(2, true, false, Set.of(bodyPart1, bodyPart2), Set.of(httpRef1, httpRef2));
        User user1 = dataHelper.createUser("one", role, country1, Set.of(exercise1, exercise2), 20);

        // User 2
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        BodyPart bodyPart4 = dataHelper.createBodyPart(4);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        Exercise exercise3 = dataHelper.createExercise(3, true, true, Set.of(bodyPart3), Set.of(httpRef3));
        Exercise exercise4 =
                dataHelper.createExercise(4, true, false, Set.of(bodyPart3, bodyPart4), Set.of(httpRef3, httpRef4));
        User user2 = dataHelper.createUser("two", role, country2, Set.of(exercise3, exercise4), 20);

        // Few default exercises
        Exercise exercise5 = dataHelper.createExercise(5, false, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise6 = dataHelper.createExercise(6, false, false, Set.of(bodyPart3), Set.of(httpRef3));

        CreateWorkoutRequestDto requestDto =
                dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId(), exercise2.getId()));

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(requestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andExpect(jsonPath("$.needsEquipment", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertThat(responseDto.getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(List.of(bodyPart1, bodyPart2));

        assertThat(responseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users")
                .isEqualTo(List.of(exercise1, exercise2));

        assertThat(responseDto.getExercises().get(0).getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exercise1.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exercise2.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(0).getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exercise1.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exercise2.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomWorkoutTest_shouldReturnTitleDuplicateAnd400_whenWorkoutAlreadyExists() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);

        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        Exercise exercise1 = dataHelper.createExercise(1, true, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 =
                dataHelper.createExercise(2, true, false, Set.of(bodyPart1, bodyPart2), Set.of(httpRef1, httpRef2));
        User user = dataHelper.createUser("one", role, country1, Set.of(exercise1, exercise2), 20);

        Workout workout = dataHelper.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
            }
        });
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout);
            }
        });

        CreateWorkoutRequestDto requestDto =
                dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId(), exercise2.getId()));
        requestDto.setTitle(workout.getTitle());

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.TITLE_DUPLICATE.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomWorkoutTest_shouldReturnInvalidNestedObjectAnd400_whenExerciseNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);

        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        Exercise exercise1 = dataHelper.createExercise(1, true, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 =
                dataHelper.createExercise(2, true, false, Set.of(bodyPart1, bodyPart2), Set.of(httpRef1, httpRef2));
        User user = dataHelper.createUser("one", role, country1, Set.of(exercise1, exercise2), 20);

        CreateWorkoutRequestDto requestDto =
                dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId(), exercise2.getId(), 3L));

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.INVALID_NESTED_OBJECT.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomWorkoutTest_shouldReturnUserResourceMismatchAnd400_whenExerciseBelongsToAnotherUser()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        Country country2 = dataHelper.createCountry(2);

        // User 1
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        Exercise exercise1 = dataHelper.createExercise(1, true, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 =
                dataHelper.createExercise(2, true, false, Set.of(bodyPart1, bodyPart2), Set.of(httpRef1, httpRef2));
        User user1 = dataHelper.createUser("one", role, country1, Set.of(exercise1, exercise2), 20);

        // User 2
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        BodyPart bodyPart4 = dataHelper.createBodyPart(4);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        Exercise exercise3 = dataHelper.createExercise(3, true, true, Set.of(bodyPart3), Set.of(httpRef3));
        Exercise exercise4 =
                dataHelper.createExercise(4, true, false, Set.of(bodyPart3, bodyPart4), Set.of(httpRef3, httpRef4));
        User user2 = dataHelper.createUser("two", role, country2, Set.of(exercise3, exercise4), 20);

        CreateWorkoutRequestDto requestDto =
                dataUtil.createWorkoutRequestDto(1, List.of(exercise1.getId(), exercise2.getId(), exercise3.getId()));

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldReturnWorkoutResponseDtoAnd200_whenValidRequestProvided() throws Exception {
        // Given
        BodyPart bodyPart1 = dataHelper.createBodyPart(1);
        BodyPart bodyPart2 = dataHelper.createBodyPart(2);
        BodyPart bodyPart3 = dataHelper.createBodyPart(3);
        BodyPart bodyPart4 = dataHelper.createBodyPart(4);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, false);

        Exercise exercise1 = dataHelper.createExercise(1, true, true, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 = dataHelper.createExercise(2, true, true, Set.of(bodyPart2), Set.of(httpRef2));
        Exercise exercise3 = dataHelper.createExercise(3, true, true, Set.of(bodyPart3), Set.of(httpRef3));
        Exercise exercise4 = dataHelper.createExercise(4, false, true, Set.of(bodyPart4), Set.of(httpRef4));

        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser(
                "one",
                role,
                country1,
                new HashSet<>() {
                    {
                        add(exercise1);
                        add(exercise2);
                        add(exercise3);
                        add(exercise4);
                    }
                },
                20);

        Workout workout = dataHelper.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise1);
                add(exercise2);
            }
        });
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout);
            }
        });

        UpdateWorkoutRequestDto requestDto =
                dataUtil.updateWorkoutRequestDto(1, List.of(exercise2.getId(), exercise3.getId(), exercise4.getId()));

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        MvcResult mvcResult = mockMvc.perform(patch(REQUEST_URL, workout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(requestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andExpect(jsonPath("$.needsEquipment", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutResponseDto>() {});

        assertThat(responseDto.getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(List.of(bodyPart2, bodyPart3, bodyPart4));

        assertThat(responseDto.getExercises())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "httpRefs", "bodyParts")
                .isEqualTo(List.of(exercise2, exercise3, exercise4));

        assertThat(responseDto.getExercises().get(0).getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exercise2.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exercise3.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(2).getBodyParts())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exercise4.getBodyParts().stream()
                        .sorted(Comparator.comparingLong(BodyPart::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(0).getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exercise2.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(1).getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exercise3.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());

        assertThat(responseDto.getExercises().get(2).getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exercise4.getHttpRefs().stream()
                        .sorted(Comparator.comparingLong(HttpRef::getId))
                        .toList());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowEmptyRequestExceptionAnd400_whenEmptyDtoProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());
        requestDto.setTitle(null);
        requestDto.setDescription(null);

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.EMPTY_REQUEST.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowNotFoundExceptionAnd400_whenWorkoutNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenUserDoesntHaveWorkouts() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);

        Workout workout = dataHelper.createWorkout(1, true, null);

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, workout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenWorkoutDoesntBelongToUser()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);

        Workout workout1 = dataHelper.createWorkout(1, true, null);
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout1);
            }
        });

        Workout workout2 = dataHelper.createWorkout(2, true, null);

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, workout2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowTitleDuplicateAnd400_whenWorkoutTitleDuplicated() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);

        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout1);
                add(workout2);
            }
        });

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, Collections.emptyList());
        workout2.setTitle(requestDto.getTitle());
        dataHelper.updateWorkout(workout2);

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, workout1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.TITLE_DUPLICATE.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowInvalidNestedObjectAnd400_whenExerciseNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        Exercise exercise = dataHelper.createExercise(1, true, true, Set.of(bodyPart), Set.of(httpRef));
        Workout workout = dataHelper.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise);
            }
        });
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout);
            }
        });

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, List.of(exercise.getId() + 1));

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, workout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.INVALID_NESTED_OBJECT.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenExerciseDoesntBelongToUser()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef = dataHelper.createHttpRef(1, false);
        Exercise exercise = dataHelper.createExercise(1, true, true, Set.of(bodyPart), Set.of(httpRef));
        Workout workout = dataHelper.createWorkout(1, true, new HashSet<>() {
            {
                add(exercise);
            }
        });
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout);
            }
        });

        Exercise exercise2 = dataHelper.createExercise(2, true, true, Set.of(bodyPart), Set.of(httpRef));
        User user2 = dataHelper.createUser("two", role, country1, Set.of(exercise2), 20);
        dataHelper.userAddExercises(user2, new HashSet<>() {
            {
                add(exercise2);
            }
        });

        UpdateWorkoutRequestDto requestDto = dataUtil.updateWorkoutRequestDto(1, List.of(exercise2.getId()));

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, workout.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomWorkoutTest_shouldReturnDeletedId_whenValidUserIdAndWorkoutIdProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country1 = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country1, null, 20);
        Workout workout = dataHelper.createWorkout(1, true, null);
        dataHelper.userAddWorkout(user, new HashSet<>() {
            {
                add(workout);
            }
        });

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, workout.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$", is(workout.getId().intValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomWorkoutTest_shouldThrowNotFoundAnd400_whenDefaultWorkoutRequestedToDelete() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        User user2 = dataHelper.createUser("two", role, country, null, 20);
        Workout workout3 = dataHelper.createWorkout(3, true, null);
        dataHelper.userAddWorkout(user2, Set.of(workout3));

        Workout workout4 = dataHelper.createWorkout(4, false, null);

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, workout4.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomWorkoutTest_shouldThrowNotFoundAnd400_whenWorkoutNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        long nonExistingWorkoutId = workout2.getId() + 1000;
        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, nonExistingWorkoutId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomWorkoutTest_shouldThrowUserResourceMismatchAnd400_whenWorkoutDoesntBelongToUser()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        User user1 = dataHelper.createUser("one", role, country, null, 18);
        Workout workout1 = dataHelper.createWorkout(1, true, null);
        Workout workout2 = dataHelper.createWorkout(2, true, null);
        dataHelper.userAddWorkout(user1, Set.of(workout1, workout2));

        User user2 = dataHelper.createUser("two", role, country, null, 20);
        Workout workout3 = dataHelper.createWorkout(3, true, null);
        dataHelper.userAddWorkout(user2, Set.of(workout3));

        Workout workout4 = dataHelper.createWorkout(4, false, null);

        String REQUEST_URL = URL + "/{workoutId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, workout3.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }
}
