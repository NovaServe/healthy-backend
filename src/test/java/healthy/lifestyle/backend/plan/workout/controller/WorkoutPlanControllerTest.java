package healthy.lifestyle.backend.plan.workout.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanResponseDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.User;
import java.time.LocalDate;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
class WorkoutPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DbUtil dbUtil;

    @Autowired
    DtoUtil dtoUtil;

    @MockBean
    FirebaseMessaging firebaseMessaging;

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
    void createWorkoutPlan_shouldCreateWorkoutPlanAndReturnResponseDto_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), Collections.emptyList(), user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.workoutId", is(workout.getId().intValue())))
                //                .andExpect(jsonPath("$.startDate", is(requestDto.getStartDate())))
                //                .andExpect(jsonPath("$.endDate", is(requestDto.getEndDate())))
                .andExpect(jsonPath("$.jsonDescription", is(notNullValue())))
                .andExpect(jsonPath("$.createdAt", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        WorkoutPlanResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<WorkoutPlanResponseDto>() {});

        assertEquals(requestDto.getStartDate(), responseDto.getStartDate());
        assertEquals(requestDto.getEndDate(), responseDto.getEndDate());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenWorkoutAlreadyHasActivePlan() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), Collections.emptyList(), user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlan workoutPlanAlreadyExists = dbUtil.createWorkoutPlan(1L, user, workout);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is(ErrorMessage.ALREADY_EXISTS.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenWorkoutNotFound() throws Exception {
        // Given
        dbUtil.createUser(1);
        long wrongWorkoutId = 1000L;
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, wrongWorkoutId);
        ApiException expectedException =
                new ApiException(ErrorMessage.WORKOUT_NOT_FOUND, wrongWorkoutId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("createWorkoutPlanInvalidDate")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenInvalidDate(LocalDate startDate, LocalDate endDate)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), Collections.emptyList(), user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());
        requestDto.setStartDate(startDate);
        requestDto.setEndDate(endDate);
        ApiException expectedException = new ApiException(ErrorMessage.INCORRECT_TIME, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    static Stream<Arguments> createWorkoutPlanInvalidDate() {
        return Stream.of(
                // Invalid start date
                Arguments.of(LocalDate.now().minusDays(1), LocalDate.now().plusDays(7)),
                // Invalid end date
                Arguments.of(LocalDate.now().plusDays(1), LocalDate.now()));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenBlankFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), Collections.emptyList(), user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());
        requestDto.setWorkoutId(null);
        requestDto.setStartDate(null);
        requestDto.setEndDate(null);
        requestDto.setJsonDescription(null);

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.workoutId", is("Id must be equal or greater than 0")))
                .andExpect(jsonPath("$.startDate", is("must not be null")))
                .andExpect(jsonPath("$.endDate", is("must not be null")))
                .andExpect(jsonPath("$.jsonDescription", is("must not be blank")))
                .andDo(print());
    }
}
