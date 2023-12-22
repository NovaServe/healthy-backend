package healthy.lifestyle.backend.users.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
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

    @ParameterizedTest
    @MethodSource("signupAgeIsOptional")
    void signupTest_shouldReturnVoidWith201_whenValidRequest(Integer ageIsOptional) throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId());
        requestDto.setAge(ageIsOptional);

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    static Stream<Arguments> signupAgeIsOptional() {
        return Stream.of(Arguments.of(20), Arguments.of((Object) null));
    }

    @Test
    void signupTest_shouldReturnErrorMessageWith400_whenUserAlreadyExists() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User alreayExistentUser = dbUtil.createUser(1, role, country);
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

    @ParameterizedTest
    @MethodSource("signupOneInputFieldIsInvalid")
    void signupTest_shouldReturnValidationMessageWith400_whenOneInputFieldIsInvalid(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword,
            String errorFieldName,
            String errorMessage)
            throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId());
        if (email != null) requestDto.setEmail(email);
        if (username != null) requestDto.setUsername(username);
        if (fullName != null) requestDto.setFullName(fullName);
        if (password != null) requestDto.setPassword(password);
        if (confirmPassword != null) requestDto.setConfirmPassword(confirmPassword);
        if (countryId != null) requestDto.setCountryId(countryId);
        if (age != null) requestDto.setAge(age);

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> signupOneInputFieldIsInvalid() {
        return Stream.of(
                Arguments.of("Username%^&", null, null, null, null, null, null, "$.username", "Not allowed symbols"),
                Arguments.of(
                        null,
                        "email()@email.com",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "$.email",
                        "must be a well-formed email address"),
                Arguments.of(null, null, "Full name !@#", null, null, null, null, "$.fullName", "Not allowed symbols"),
                Arguments.of(
                        null, null, null, -1L, null, null, null, "$.countryId", "must be greater than or equal to 0"),
                Arguments.of(null, null, null, null, 15, null, null, "$.age", "Age should be in range from 16 to 120"),
                Arguments.of(null, null, null, null, 121, null, null, "$.age", "Age should be in range from 16 to 120"),
                Arguments.of(
                        null, null, null, null, null, "Password 1", "Password 1", "$.password", "Not allowed symbols"),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Password_1",
                        "Password_2",
                        "password,confirmPassword",
                        "Passwords don't match"));
    }

    @ParameterizedTest
    @MethodSource("signupOneInputFieldIsNull")
    void signupTest_shouldReturnValidationMessageWith400_whenOneInputFieldIsNull(
            String username,
            String email,
            String fullName,
            Long countryId,
            String password,
            String confirmPassword,
            String errorFieldName,
            String errorMessage)
            throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId());
        if (email == null) requestDto.setEmail(email);
        if (username == null) requestDto.setUsername(username);
        if (fullName == null) requestDto.setFullName(fullName);
        if (password == null) requestDto.setPassword(password);
        if (confirmPassword == null) requestDto.setConfirmPassword(confirmPassword);
        if (countryId == null) requestDto.setCountryId(countryId);

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> signupOneInputFieldIsNull() {
        return Stream.of(
                Arguments.of(
                        null, "Not null", "Not null", 1L, "Not null", "Not null", "$.username", "must not be blank"),
                Arguments.of("Not null", null, "Not null", 1L, "Not null", "Not null", "$.email", "must not be blank"),
                Arguments.of(
                        "Not null", "Not null", null, 1L, "Not null", "Not null", "$.fullName", "must not be blank"),
                Arguments.of(
                        "Not null",
                        "Not null",
                        "Not null",
                        null,
                        "Not null",
                        "Not null",
                        "$.countryId",
                        "must not be null"),
                Arguments.of(
                        "Not null", "Not null", "Not null", 1L, null, "Not null", "$.password", "must not be blank"),
                Arguments.of(
                        "Not null",
                        "Not null",
                        "Not null",
                        1L,
                        "Not null",
                        null,
                        "$.confirmPassword",
                        "must not be blank"));
    }

    @ParameterizedTest
    @MethodSource("signupAllInputsAreInvalid")
    void signupTest_shouldReturnValidationMessageWith400_whenAllInputsAreInvalid(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDtoEmpty();
        requestDto.setEmail(email);
        requestDto.setUsername(username);
        requestDto.setFullName(fullName);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);
        requestDto.setCountryId(countryId);
        requestDto.setAge(age);

        // When
        MvcResult mvcResult = mvcResult = mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);

        assertEquals("Not allowed symbols", responseJson.get("username").asText());
        assertEquals(
                "must be a well-formed email address", responseJson.get("email").asText());
        assertEquals("Not allowed symbols", responseJson.get("fullName").asText());
        assertEquals(
                "must be greater than or equal to 0",
                responseJson.get("countryId").asText());
        assertEquals(
                "Age should be in range from 16 to 120", responseJson.get("age").asText());
        assertEquals("Not allowed symbols", responseJson.get("password").asText());
        assertEquals("Not allowed symbols", responseJson.get("confirmPassword").asText());
    }

    static Stream<Arguments> signupAllInputsAreInvalid() {
        return Stream.of(
                Arguments.of("Username%^&", "email()@email.com", "Full name !@#", -1L, 1, "Password 1", "Password 1"));
    }

    @ParameterizedTest
    @MethodSource("signupAllInputsAreNull")
    void signupTest_shouldReturnValidationMessageWith400_whenAllInputsAreNull(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDtoEmpty();
        requestDto.setEmail(email);
        requestDto.setUsername(username);
        requestDto.setFullName(fullName);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);
        requestDto.setCountryId(countryId);
        requestDto.setAge(age);

        // When
        mockMvc.perform(post(URL.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is("must not be blank")))
                .andExpect(jsonPath("$.email", is("must not be blank")))
                .andExpect(jsonPath("$.fullName", is("must not be blank")))
                .andExpect(jsonPath("$.countryId", is("must not be null")))
                .andExpect(jsonPath("$.age").doesNotExist())
                .andExpect(jsonPath("$.password", is("must not be blank")))
                .andExpect(jsonPath("$.confirmPassword", is("must not be blank")))
                .andExpect(jsonPath("$.password,confirmPassword", is("Passwords don't match")))
                .andDo(print());
    }

    static Stream<Arguments> signupAllInputsAreNull() {
        return Stream.of(Arguments.of(null, null, null, null, null, null, null));
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

    @ParameterizedTest
    @MethodSource("loginUsernamePasswordMismatch")
    void loginTest_shouldReturnErrorMessageWith401_whenUsernamePasswordMismatch(
            String usernameOrEmail, String password, String confirmPassword) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        LoginRequestDto requestDto = dtoUtil.loginRequestDtoEmpty();
        requestDto.setUsernameOrEmail(usernameOrEmail);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Authentication error")))
                .andDo(print());
    }

    static Stream<Arguments> loginUsernamePasswordMismatch() {
        return Stream.of(
                Arguments.of("Username-1", "Password-2", "Password-2"),
                Arguments.of("Username-2", "Password-1", "Password-1"));
    }

    @ParameterizedTest
    @MethodSource("loginOneFieldIsInvalid")
    void loginTest_shouldReturnValidationMessageWith400_whenOneFieldIsInvalid(
            String usernameOrEmail, String password, String confirmPassword, String errorFieldName, String errorMessage)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        LoginRequestDto requestDto = dtoUtil.loginRequestDtoEmpty();
        requestDto.setUsernameOrEmail(usernameOrEmail);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> loginOneFieldIsInvalid() {
        return Stream.of(
                Arguments.of("Username%^&", "Password-1", "Password-1", "$.usernameOrEmail", "Not allowed symbols"),
                Arguments.of(
                        "email()@email.com", "Password-1", "Password-1", "$.usernameOrEmail", "Not allowed symbols"),
                Arguments.of("Username-1", "Password 2", "Password 2", "$.password", "Not allowed symbols"),
                Arguments.of("Username-1", "Password 2", "Password 2", "$.confirmPassword", "Not allowed symbols"),
                Arguments.of(
                        "Username-1",
                        "Password-2",
                        "Password-3",
                        "$.password,confirmPassword",
                        "Passwords don't match"));
    }

    //
    @ParameterizedTest
    @MethodSource("loginOneFieldIsNull")
    void loginTest_shouldReturnValidationMessageWith400_whenOneFieldIsNull(
            String usernameOrEmail, String password, String confirmPassword, String errorFieldName, String errorMessage)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        LoginRequestDto requestDto = dtoUtil.loginRequestDtoEmpty();
        requestDto.setUsernameOrEmail(usernameOrEmail);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        mockMvc.perform(post(URL.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> loginOneFieldIsNull() {
        return Stream.of(
                Arguments.of(null, "Password-1", "Password-1", "$.usernameOrEmail", "must not be blank"),
                Arguments.of("Username-1", null, null, "$.password", "must not be blank"),
                Arguments.of("Username-1", null, null, "$.confirmPassword", "must not be blank"),
                Arguments.of("Username-1", null, null, "$.password,confirmPassword", "Passwords don't match"));
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
