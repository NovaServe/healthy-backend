package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.exception.ExceptionDto;
import healthy.lifestyle.backend.workout.dto.WorkoutResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

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
}
