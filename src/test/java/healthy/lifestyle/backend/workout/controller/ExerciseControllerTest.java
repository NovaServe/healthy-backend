package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.GetExercisesResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * @see ExerciseController
 * @see healthy.lifestyle.backend.workout.service.ExerciseServiceImpl
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class ExerciseControllerTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            (PostgreSQLContainer<?>) new PostgreSQLContainer(DockerImageName.parse("postgres:12.15"))
                    .withDatabaseName("test_db")
                    .withUsername("test_user")
                    .withPassword("test_password")
                    .withReuse(true);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> String.format(
                        "jdbc:postgresql://localhost:%s/%s",
                        postgresqlContainer.getFirstMappedPort(), postgresqlContainer.getDatabaseName()));
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    private static final String URL = "/api/v1/exercises";

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getExercises_DefaultAndCustom() throws Exception {
        BodyPart bodyPart1 = dataHelper.createBodyPart("Body Part 1");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Body Part 2");
        BodyPart bodyPart3 = dataHelper.createBodyPart("Body Part 3");
        HttpRef httpRef1 = dataHelper.createHttpRef("Ref 1", "http://ref1.com", "Desc 1");
        HttpRef httpRef2 = dataHelper.createHttpRef("Ref 2", "http://ref2.com");
        HttpRef httpRef3 = dataHelper.createHttpRef("Ref 3", "http://ref1.com", "Desc 3");
        // Default exercises
        Exercise exercise1 =
                dataHelper.createExercise("Exercise 1", "Description 1", false, Set.of(bodyPart1), Set.of(httpRef1));
        Exercise exercise2 =
                dataHelper.createExercise("Exercise 2", "Description 2", false, Set.of(bodyPart2), Set.of(httpRef2));
        // Custom exercises
        Exercise exercise3 = dataHelper.createExercise(
                "Exercise 3", "Description 3", true, Set.of(bodyPart1, bodyPart2), Set.of(httpRef1, httpRef2));
        Exercise exercise4 =
                dataHelper.createExercise("Exercise 4", "Description 4", true, Set.of(bodyPart3), Set.of(httpRef3));
        Exercise exercise5 =
                dataHelper.createExercise("Exercise 5", "Description 5", true, Set.of(bodyPart3), Set.of(httpRef3));

        Role role = dataHelper.createRole("ROLE_USER");
        User user1 = dataHelper.createUser(
                "Test Full Name",
                "test-username",
                "test@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise2, exercise3));
        User user2 = dataHelper.createUser(
                "Test Full Name",
                "test-username-two",
                "test2@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise4));

        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        GetExercisesResponseDto responseDto = objectMapper.readValue(responseContent, GetExercisesResponseDto.class);
        assertEquals(3, responseDto.getExercises().size());

        // TODO: Check all exercises and nested body parts and http refs
    }
}
