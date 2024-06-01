package healthy.lifestyle.backend.plan.workout.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.plan.workout.dto.WorkoutPlanCreateRequestDto;
import healthy.lifestyle.backend.plan.workout.model.WorkoutPlan;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

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
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), null, user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workoutId", is(workout.getId())))
                .andExpect(jsonPath("$.startDate", is(requestDto.getStartDate())))
                .andExpect(jsonPath("$.endDate", is(requestDto.getEndDate())))
                .andExpect(jsonPath("$.jsonDescription", is(requestDto.getJsonDescription())))
                .andExpect(jsonPath("$.createdAt", is(notNullValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenWorkoutAlreadyHasActivePlan() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), null, user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlan workoutPlanAlreadyExistent = dbUtil.createWorkoutPlan(1L, user, workout);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
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

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.WORKOUT_NOT_FOUND.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenInvalidDates() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), null, user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());
        requestDto.setStartDate(LocalDateTime.now().plusDays(1));
        requestDto.setEndDate(LocalDateTime.now().minusDays(1));

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                //                .andExpect(jsonPath("$.message", is("validation message")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenInvalidStartDate() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), null, user);
        Workout workout = dbUtil.createCustomWorkout(1, List.of(exercise), user);
        WorkoutPlanCreateRequestDto requestDto = dtoUtil.workoutPlanCreateRequestDto(1, workout.getId());
        requestDto.setStartDate(LocalDateTime.now().minusDays(1));
        requestDto.setEndDate(LocalDateTime.now().plusDays(1));

        // When
        mockMvc.perform(post(URL.WORKOUT_PLANS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.INCORRECT_TIME.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createWorkoutPlan_shouldThrowException_whenBlankFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart = dbUtil.createBodyPart(1);
        Exercise exercise = dbUtil.createCustomExercise(1, true, List.of(bodyPart), null, user);
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
                //                .andExpect(jsonPath("$.message", is("validation message")))
                .andDo(print());
    }
}
