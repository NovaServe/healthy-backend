package healthy.lifestyle.backend.users.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.users.dto.LoginRequestDto;
import healthy.lifestyle.backend.users.dto.LoginResponseDto;
import healthy.lifestyle.backend.users.dto.SignupRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.URL;
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
@Import(BeanConfig.class)
class AuthControllerTest {
    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

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
    DbUtil dbUtil;

    @Autowired
    DtoUtil dtoUtil;

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    void signupTest_shouldReturnVoidWith201_whenValidRequest() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId());

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenUserAlreadyExists() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User alreayExistedUser = dbUtil.createUser(1, role, country);
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId());

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Already exists")))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenInvalidUsername() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);
        signupRequestDto.setUsername("username 123 $");

        // When
        mockMvc.perform(post(URL.SIGNUP)
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
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);
        signupRequestDto.setEmail("invalid-email-123-$@email.com");

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenInvalidPassword() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);
        signupRequestDto.setPassword("Invalid password with space");
        signupRequestDto.setConfirmPassword("Invalid password with space");

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is(equalTo("Not allowed symbols"))))
                .andExpect(jsonPath("$.confirmPassword", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenPasswordsMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);
        signupRequestDto.setConfirmPassword("Password mismatch");

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is(equalTo("Passwords don't match"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenInvalidFullName() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);
        signupRequestDto.setFullName("Invalid Full Name &");

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fullName", is(equalTo("Not allowed symbols"))))
                .andDo(print());
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenInvalidAge() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 3;
        SignupRequestDto signupRequestDto = dtoUtil.signupRequestDto(1, country.getId(), age);

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.age", is(equalTo("Not allowed age, should be in 5-200"))))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnTokenWith200_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        LoginRequestDto requestDto = dtoUtil.loginRequestDto(1);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
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
    void loginTest_shouldReturnErrorMessageWith401_whenUserNotFound() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dtoUtil.loginRequestDto(1);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Authentication error")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnErrorMessageWith401_whenWrongPassword() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        User user = dbUtil.createUser(1, role, country);

        LoginRequestDto loginRequestDto = dtoUtil.loginRequestDto(1);
        loginRequestDto.setPassword("Wrong-password");
        loginRequestDto.setConfirmPassword("Wrong-password");

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Authentication error")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnErrorMessageWith401_whenInvalidUsername() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dtoUtil.loginRequestDto(1);
        loginRequestDto.setUsernameOrEmail("Invalid username 123 $");

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.usernameOrEmail", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnErrorMessageWith401_whenInvalidPassword() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dtoUtil.loginRequestDto(1);
        loginRequestDto.setPassword("Invalid password with spaces");

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    void loginTest_shouldReturnErrorMessageWith401_whenPasswordsMismatch() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = dtoUtil.loginRequestDto(1);
        loginRequestDto.setConfirmPassword("Password mismatch");

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is("Passwords don't match")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void validateTokenTest_shouldReturnVoidWith200_whenTokenIsValid() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Integer age = 20;
        User user = dbUtil.createUser(1, role, country);

        // When
        mockMvc.perform(get(URL.VALIDATE).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void validateTokenTest_shouldReturnErrorMessageWith401_whenUnauthorized() throws Exception {
        // When
        mockMvc.perform(get(URL.VALIDATE).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}
