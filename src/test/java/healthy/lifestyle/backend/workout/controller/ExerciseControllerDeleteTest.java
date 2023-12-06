package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.data.bodypart.BodyPartJpaTestBuilder;
import healthy.lifestyle.backend.data.exercise.ExerciseDtoTestBuilder;
import healthy.lifestyle.backend.data.httpref.HttpRefJpaTestBuilder;
import healthy.lifestyle.backend.data.user.UserJpaTestBuilder;
import healthy.lifestyle.backend.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import({DataConfiguration.class})
public class ExerciseControllerDeleteTest {
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

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void deleteCustomExerciseTest_shouldReturnDeletedId_whenValidRequestProvided() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExercises(2)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddMultipleExercises();

        long exerciseIdToBeRemoved = userWrapper.getExerciseIdFromSortedList(0);

        // When
        mockMvc.perform(delete(REQUEST_URL, userWrapper.getExerciseIdFromSortedList(0))
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath(
                        "$", is(userWrapper.getExerciseIdFromSortedList(0).intValue())))
                .andDo(print());

        assertEquals(
                1,
                userWrapper.getUserById(userWrapper.getUserId()).getExercises().size());
        assertNotEquals(exerciseIdToBeRemoved, userWrapper.getExerciseIdFromSortedList(1));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "password-one", roles = "USER")
    void deleteCustomExerciseTest_shouldReturnErrorMessageAnd400_whenWrongExerciseIdGiven() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setExerciseIdOrSeed(1)
                .setIsExerciseCustom(true)
                .setIsExerciseNeedsEquipment(true)
                .setAmountOfExercises(2)
                .setAmountOfExerciseNestedEntities(4)
                .setStartIdOrSeedForExerciseNestedEntities(1)
                .setIsExerciseHttpRefsCustom(false)
                .buildUserAndAddMultipleExercises();

        long wrongExerciseId = 1000L;

        // When
        mockMvc.perform(delete(REQUEST_URL, wrongExerciseId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andDo(print());
    }
}
