package healthy.lifestyle.backend.users.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * @see AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class AuthControllerTest {
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

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    private static final String URL = "/api/v1/users/auth";

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    void signup_Positive() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-username")
                .email("test@email.com")
                .password("test-password")
                .confirmPassword("test-password")
                .fullName("Test Full Name")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print());
    }

    @Test
    void signup_Negative_InvalidUsername() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-usernam e")
                .email("test@email.com")
                .password("test-password")
                .confirmPassword("test-password")
                .fullName("Test Full Name")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signup_Negative_InvalidEmail() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-username")
                .email("test$@email.com")
                .password("test-password")
                .confirmPassword("test-password")
                .fullName("Test Full Name")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signup_Negative_InvalidPassword() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-username")
                .email("test@email.com")
                .password("test password")
                .confirmPassword("test password")
                .fullName("Test Full Name")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is(equalTo("Not allowed symbols"))))
                .andExpect(jsonPath("$.confirmPassword", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signup_Negative_PasswordsMismatch() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-username")
                .email("test@email.com")
                .password("test-password")
                .confirmPassword("test_password")
                .fullName("Test Full Name")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is(equalTo("Passwords don't match"))))
                .andDo(print());
    }

    @Test
    void signup_Negative_InvalidFullname() throws Exception {
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto requestDto = new SignupRequestDto.Builder()
                .username("test-username")
                .email("test@email.com")
                .password("test-password")
                .confirmPassword("test-password")
                .fullName("Test Full Name +")
                .build();

        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }
}
