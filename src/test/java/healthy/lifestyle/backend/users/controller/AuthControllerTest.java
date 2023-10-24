package healthy.lifestyle.backend.users.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(DataConfiguration.class)
class AuthControllerTest {
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
    DataUtil dataUtil;

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
    void signupTest_shouldReturn201Created() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenUserAlreadyExists() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser("one", role, null);
        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Already exists")))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenInvalidUsername() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");
        signupRequestDto.setUsername("username 123 $");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenInvalidEmail() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");
        signupRequestDto.setEmail("invalid-email-123-$@email.com");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenInvalidPassword() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");
        signupRequestDto.setPassword("Invalid password with space");
        signupRequestDto.setConfirmPassword("Invalid password with space");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is(equalTo("Not allowed symbols"))))
                .andExpect(jsonPath("$.confirmPassword", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenPasswordsMismatch() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");
        signupRequestDto.setConfirmPassword("Password mismatch");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is(equalTo("Passwords don't match"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturn400BadRequest_whenInvalidFullName() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        SignupRequestDto signupRequestDto = dataUtil.createSignupRequestDto("one");
        signupRequestDto.setFullName("Invalid Full Name &");

        // When
        mockMvc.perform(post(URL + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnTokenAnd200Ok() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser("one", role, null);

        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        LoginResponseDto responseDto = objectMapper.readValue(responseContent, LoginResponseDto.class);
        String token = responseDto.getToken();
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
        assertTrue(tokenParts[0].length() >= 10);
        assertTrue(tokenParts[1].length() >= 10);
        assertTrue(tokenParts[2].length() >= 10);
    }

    @Test
    void loginTest_shouldReturn401Unauthorized_whenUserNotFound() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");

        // When
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Authentication error")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturn401Unauthorized_whenWrongPassword() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser("one", role, null);

        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");
        loginRequestDto.setPassword("Wrong-password");
        loginRequestDto.setConfirmPassword("Wrong-password");

        // When
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Authentication error")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturn401Unauthorized_whenInvalidUsername() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");
        loginRequestDto.setUsernameOrEmail("Invalid username 123 $");

        // When
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.usernameOrEmail", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturn401Unauthorized_whenInvalidPassword() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");
        loginRequestDto.setPassword("Invalid password with spaces");

        // When
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturn401Unauthorized_whenPasswordsMismatch() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dataUtil.createLoginRequestDto("one");
        loginRequestDto.setConfirmPassword("Password mismatch");

        // When
        mockMvc.perform(post(URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is("Passwords don't match")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void validateTokenTest_shouldReturn200Ok_whenTokenIsValid() throws Exception {
        // Given
        String URL_POSTFIX = "/validate";
        Role role = dataHelper.createRole("ROLE_USER");
        User user = dataHelper.createUser("one", role, null);

        // When
        mockMvc.perform(get(URL + URL_POSTFIX).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void validateTokenTest_shouldReturn401Unauthorized_whenUnauthorized() throws Exception {
        // Given
        String URL_POSTFIX = "/validate";

        // When
        mockMvc.perform(get(URL + URL_POSTFIX).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
