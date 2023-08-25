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
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import java.util.List;
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
class BodyPartControllerTest {
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

    private static final String URL = "/api/v1/exercises/bodyParts";

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getBodyPartsPositive() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser(
                "Test Full Name",
                "test-username",
                "test@email.com",
                passwordEncoder().encode("test-password"),
                role,
                null);
        BodyPart bodyPart1 = dataHelper.createBodyPart("Body Part 1");
        BodyPart bodyPart2 = dataHelper.createBodyPart("Body Part 2");

        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<BodyPartResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<BodyPartResponseDto>>() {});
        assertEquals(2, responseDto.size());
        assertEquals(bodyPart1.getId(), responseDto.get(0).getId());
        assertEquals(bodyPart1.getName(), responseDto.get(0).getName());
        assertEquals(bodyPart2.getId(), responseDto.get(1).getId());
        assertEquals(bodyPart2.getName(), responseDto.get(1).getName());
    }

    @Test
    @WithMockUser(username = "test-username", password = "test-password", roles = "USER")
    void getBodyPartsNegative() throws Exception {
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
    void getBodyPartsNegativeNoAccess() throws Exception {
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
