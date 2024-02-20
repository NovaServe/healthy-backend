package healthy.lifestyle.backend.user.controller;

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
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.user.dto.LoginRequestDto;
import healthy.lifestyle.backend.user.dto.LoginResponseDto;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.validation.UserValidationMessage;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.URL;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    void login_shouldReturnTokenWith200_whenValidRequest() throws Exception {
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

    @ParameterizedTest
    @MethodSource("loginWrongCredentials")
    void login_shouldReturnErrorMessageWith401_whenWrongCredentials(String usernameOrEmail, String password)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        ApiException expectedException =
                new ApiException(ErrorMessage.AUTHENTICATION_ERROR, null, HttpStatus.UNAUTHORIZED);

        LoginRequestDto requestDto = dtoUtil.loginRequestDtoEmpty();
        requestDto.setUsernameOrEmail(usernameOrEmail);
        requestDto.setPassword(password);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    static Stream<Arguments> loginWrongCredentials() {
        return Stream.of(
                Arguments.of("Username-1", "Password-2"),
                Arguments.of("Username-2", "Password-1"),
                Arguments.of("email-1@email.com", "Password-2"),
                Arguments.of("email-2@email.com", "Password-1"));
    }

    @ParameterizedTest
    @MethodSource("loginInvalidUsernameOrEmailOrPassword")
    void login_shouldReturnValidationMessageWith400_whenInvalidUsernameOrEmailOrPassword(
            String usernameOrEmail, String password, String errorField, String errorMessage) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        LoginRequestDto requestDto = dtoUtil.loginRequestDtoEmpty();
        requestDto.setUsernameOrEmail(usernameOrEmail);
        requestDto.setPassword(password);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + errorField, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> loginInvalidUsernameOrEmailOrPassword() {
        return Stream.of(
                Arguments.of(null, "Password-1", "usernameOrEmail", "Must not be null"),
                Arguments.of("", "Password-1", "usernameOrEmail", "Must not be blank"),
                Arguments.of(" ", "Password-1", "usernameOrEmail", "Must not be blank"),
                Arguments.of(
                        "username!",
                        "Password-1",
                        "usernameOrEmail",
                        "Username can include lower and upper-case letters, digits, and symbols: . - _"),
                Arguments.of(
                        "user", "Password-1", "usernameOrEmail", UserValidationMessage.USERNAME_LENGTH_RANGE.getName()),
                Arguments.of(
                        "email@@email..com",
                        "Password-1",
                        "usernameOrEmail",
                        "Email can contain lower and upper-case letters, digits, and symbols: . - _"),
                Arguments.of(
                        "email!@email.com",
                        "Password-1",
                        "usernameOrEmail",
                        "Email can contain lower and upper-case letters, digits, and symbols: . - _"),
                Arguments.of(
                        "email@", "Password-1", "usernameOrEmail", UserValidationMessage.EMAIL_LENGTH_RANGE.getName()),
                Arguments.of("username-valid", null, "password", UserValidationMessage.PASSWORD_LENGTH_RANGE.getName()),
                Arguments.of("username-valid", "", "password", UserValidationMessage.PASSWORD_LENGTH_RANGE.getName()),
                Arguments.of("username-valid", " ", "password", UserValidationMessage.PASSWORD_LENGTH_RANGE.getName()),
                Arguments.of(
                        "username-valid",
                        "password",
                        "password",
                        UserValidationMessage.PASSWORD_LENGTH_RANGE.getName()),
                Arguments.of(
                        "username-valid",
                        "password with space",
                        "password",
                        "Password can include lower and upper-case letters, digits, and symbols: . , - _ < > : ; ! ? # $ % ^ & * ( ) + ="));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void validateToken_shouldReturnVoidWith200_whenTokenIsValid() throws Exception {
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
