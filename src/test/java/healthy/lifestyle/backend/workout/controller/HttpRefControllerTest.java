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

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class HttpRefControllerTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
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
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnDefaultAndCustomHttpRefsAnd200Ok_whenUserAuthorized() throws Exception {
        // Given
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, false);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);

        Exercise exercise1 = dataHelper.createExercise(1, true, Set.of(bodyPart), Set.of(httpRef2));

        Role role = dataHelper.createRole("ROLE_USER");
        User user1 = dataHelper.createUser("one", role, Set.of(exercise1));

        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        Exercise exercise2 = dataHelper.createExercise(2, true, Set.of(bodyPart), Set.of(httpRef1, httpRef3));
        User user2 = dataHelper.createUser("two", role, Set.of(exercise2));

        Exercise exercise3 = dataHelper.createExercise(3, false, Set.of(bodyPart), Set.of(httpRef1));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
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
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnDefaultHttpRefsAnd200Ok_whenUserNotAuthorized() throws Exception {
        // Given
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, false);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, false);

        Exercise exercise1 = dataHelper.createExercise(1, true, Set.of(bodyPart), Set.of(httpRef2));

        Role role = dataHelper.createRole("ROLE_USER");
        User user1 = dataHelper.createUser("one", role, Set.of(exercise1));

        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        Exercise exercise2 = dataHelper.createExercise(2, true, Set.of(bodyPart), Set.of(httpRef1, httpRef3));

        User user2 = dataHelper.createUser("two", role, Set.of(exercise2));

        Exercise exercise3 = dataHelper.createExercise(3, false, Set.of(bodyPart), Set.of(httpRef1));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
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
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnErrorMessageAnd500InternalServerError_whenNoHttpRefs() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser("one", role, null);

        // When
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }

    @Test
    void getHttpRefsTest_shouldReturn401Unauthorized_whenUserNotAuthorized() throws Exception {
        // When
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
