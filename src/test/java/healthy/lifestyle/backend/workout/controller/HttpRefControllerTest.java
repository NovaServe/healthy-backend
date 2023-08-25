package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
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
 * @see HttpRefController
 * @see healthy.lifestyle.backend.workout.service.HttpRefServiceImpl
 * @see healthy.lifestyle.backend.workout.dto.HttpRefResponseDto
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class HttpRefControllerTest {
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

    private static final String URL = "/api/v1/exercises/httpRefs";

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getHttpRefsPositiveDefaultAndCustom() throws Exception {
        // Test subject
        BodyPart bodyPart = dataHelper.createBodyPart("Body 1");
        HttpRef httpRef1 = dataHelper.createHttpRef("Name 1", "Ref 1", "Desc 1", false);
        HttpRef httpRef2 = dataHelper.createHttpRef("Name 2", "Ref 2", "Desc 2", true);
        Exercise exercise1 = dataHelper.createExercise("Title 1", "Desc 1", true, Set.of(bodyPart), Set.of(httpRef2));
        Role role = dataHelper.createRole("ROLE_USER");
        User user1 = dataHelper.createUser(
                "Test Full Name",
                "test-username",
                "test@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise1));

        // Other
        HttpRef httpRef3 = dataHelper.createHttpRef("Name 3", "Ref 3", "Desc 3", true);
        Exercise exercise2 =
                dataHelper.createExercise("Title 2", "Desc 2", true, Set.of(bodyPart), Set.of(httpRef1, httpRef3));
        User user2 = dataHelper.createUser(
                "Test Full Name Two",
                "test-username-two",
                "test2@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise2));

        Exercise exercise3 = dataHelper.createExercise("Title 3", "Desc 3", false, Set.of(bodyPart), Set.of(httpRef1));

        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(httpRef1.getId(), responseDto.get(0).getId());
        assertEquals(httpRef1.getName(), responseDto.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDto.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDto.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDto.get(1).getId());
        assertEquals(httpRef2.getName(), responseDto.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDto.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDto.get(1).getDescription());
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getHttpRefsPositiveDefaultOnly() throws Exception {
        // Test subject
        BodyPart bodyPart = dataHelper.createBodyPart("Body 1");
        HttpRef httpRef1 = dataHelper.createHttpRef("Name 1", "Ref 1", "Desc 1", false);
        HttpRef httpRef2 = dataHelper.createHttpRef("Name 2", "Ref 2", "Desc 2", false);
        Exercise exercise1 = dataHelper.createExercise("Title 1", "Desc 1", true, Set.of(bodyPart), Set.of(httpRef2));
        Role role = dataHelper.createRole("ROLE_USER");
        User user1 = dataHelper.createUser(
                "Test Full Name",
                "test-username",
                "test@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise1));

        // Other
        HttpRef httpRef3 = dataHelper.createHttpRef("Name 3", "Ref 3", "Desc 3", true);
        Exercise exercise2 =
                dataHelper.createExercise("Title 2", "Desc 2", true, Set.of(bodyPart), Set.of(httpRef1, httpRef3));
        User user2 = dataHelper.createUser(
                "Test Full Name Two",
                "test-username-two",
                "test2@email.com",
                passwordEncoder().encode("test-password"),
                role,
                Set.of(exercise2));

        Exercise exercise3 = dataHelper.createExercise("Title 3", "Desc 3", false, Set.of(bodyPart), Set.of(httpRef1));

        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(httpRef1.getId(), responseDto.get(0).getId());
        assertEquals(httpRef1.getName(), responseDto.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDto.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDto.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDto.get(1).getId());
        assertEquals(httpRef2.getName(), responseDto.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDto.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDto.get(1).getDescription());
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getHttpRefsNegativeEmptyRepository() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser(
                "Test Full Name",
                "test-username",
                "test@email.com",
                passwordEncoder().encode("test-password"),
                role,
                null);

        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }

    @Test
    void getHttpRefsNegativeUnauthorized() throws Exception {
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
