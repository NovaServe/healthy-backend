package healthy.lifestyle.backend.users.controller;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.country.CountryJpaTestBuilder;
import healthy.lifestyle.backend.data.user.UserDtoTestBuilder;
import healthy.lifestyle.backend.data.user.UserJpaTestBuilder;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.exception.ExceptionDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
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
public class UserUpdateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    UserJpaTestBuilder userJpaTestBuilder;

    @Autowired
    CountryJpaTestBuilder countryJpaTestBuilder;

    @Autowired
    UserDtoTestBuilder userDtoTestBuilder;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DataHelper dataHelper;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String URL = "/api/v1/users/{userId}";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputs")
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnUserResponseDto_whenValidInputGiven(
            String username,
            String email,
            String fullName,
            String countryName,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        CountryJpaTestBuilder.CountryTestWrapper countryWrapper = null;
        if (nonNull(countryName)) {
            countryWrapper = countryJpaTestBuilder.getWrapper();
            countryWrapper.setIdOrSeed(2L).build();
        }

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        if (nonNull(countryName)) userRequestDtoWrapper.setFieldValue("countryId", countryWrapper.getId());
        else userRequestDtoWrapper.setFieldValue("countryId", userWrapper.getCountryId());
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(age)) userRequestDtoWrapper.setFieldValue("age", age);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL, userWrapper.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        UserResponseDto responseDto = objectMapper.readValue(responseContent, UserResponseDto.class);

        if (nonNull(username)) assertEquals(userRequestDtoWrapper.getFieldValue("username"), responseDto.getUsername());
        else assertEquals(userWrapper.getFieldValue("username"), responseDto.getUsername());

        if (nonNull(email)) assertEquals(userRequestDtoWrapper.getFieldValue("email"), responseDto.getEmail());
        else assertEquals(userWrapper.getFieldValue("email"), responseDto.getEmail());

        if (nonNull(fullName)) assertEquals(userRequestDtoWrapper.getFieldValue("fullName"), responseDto.getFullName());
        else assertEquals(userWrapper.getFieldValue("fullName"), responseDto.getFullName());

        assertEquals(userRequestDtoWrapper.getFieldValue("countryId"), responseDto.getCountryId());

        if (nonNull(age)) assertEquals(userRequestDtoWrapper.getFieldValue("age"), responseDto.getAge());
        else assertEquals(userWrapper.getFieldValue("age"), responseDto.getAge());

        if (nonNull(password) && nonNull(confirmPassword))
            assertTrue(passwordEncoder.matches(
                    (String) userRequestDtoWrapper.getFieldValue("password"),
                    userWrapper.getUserById(userWrapper.getUserId()).getPassword()));
    }

    static Stream<Arguments> updateUserMultipleValidInputs() {
        return Stream.of(
                Arguments.of("new-username", null, null, null, null, null, null),
                Arguments.of(null, "new-email@email.com", null, null, null, null, null),
                Arguments.of(null, null, "New full name", null, null, null, null),
                Arguments.of(null, null, null, "Country-2", null, null, null),
                Arguments.of(null, null, null, null, 110, null, null),
                Arguments.of(null, null, null, null, null, "new-password", "new-password"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputsAreNotDifferent")
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnErrorMessageAnd400_whenValidButNotDifferentInputGiven(
            String username, String email, String fullName, Integer age, String password, String confirmPassword)
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();
        if (nonNull(age)) userWrapper.setFieldValue("age", age);

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        userRequestDtoWrapper.setFieldValue("countryId", userWrapper.getCountryId());
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(age)) userRequestDtoWrapper.setFieldValue("age", age);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL, userWrapper.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExceptionDto exceptionDto = objectMapper.readValue(responseContent, ExceptionDto.class);

        boolean allFieldsAreNotDifferent = nonNull(username)
                && nonNull(email)
                && nonNull(fullName)
                && nonNull(age)
                && nonNull(password)
                && nonNull(confirmPassword);
        if (allFieldsAreNotDifferent) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append(ErrorMessage.USERNAME_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.EMAIL_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.FULLNAME_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.AGE_IS_NOT_DIFFERENT.getName());
            errorMessage.append(" ");
            errorMessage.append(ErrorMessage.PASSWORD_IS_NOT_DIFFERENT.getName());
            assertEquals(errorMessage.toString(), exceptionDto.getMessage());
        } else {
            if (nonNull(username))
                assertEquals(ErrorMessage.USERNAME_IS_NOT_DIFFERENT.getName(), exceptionDto.getMessage());
            if (nonNull(email)) assertEquals(ErrorMessage.EMAIL_IS_NOT_DIFFERENT.getName(), exceptionDto.getMessage());
            if (nonNull(fullName))
                assertEquals(ErrorMessage.FULLNAME_IS_NOT_DIFFERENT.getName(), exceptionDto.getMessage());
            if (nonNull(age)) assertEquals(ErrorMessage.AGE_IS_NOT_DIFFERENT.getName(), exceptionDto.getMessage());
            if (nonNull(password) && nonNull(confirmPassword))
                assertEquals(ErrorMessage.PASSWORD_IS_NOT_DIFFERENT.getName(), exceptionDto.getMessage());
        }
    }

    static Stream<Arguments> updateUserMultipleValidInputsAreNotDifferent() {
        return Stream.of(
                Arguments.of("Username-1", null, null, null, null, null),
                Arguments.of(null, "email-1@email.com", null, null, null, null),
                Arguments.of(null, null, "Full name one", null, null, null),
                Arguments.of(null, null, null, 20, null, null),
                Arguments.of(null, null, null, null, "Password-1", "Password-1"),
                Arguments.of("Username-1", "email-1@email.com", "Full name one", 20, "Password-1", "Password-1"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleInvalidInputs")
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnErrorMessageAnd400_whenInvalidInputGiven(
            String username,
            String email,
            String fullName,
            Integer countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();
        if (nonNull(age)) userWrapper.setFieldValue("age", age);

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        if (nonNull(countryId)) userRequestDtoWrapper.setFieldValue("countryId", null);
        else userRequestDtoWrapper.setFieldValue("countryId", userWrapper.getCountryId());
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(age)) userRequestDtoWrapper.setFieldValue("age", age);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL, userWrapper.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);

        boolean allFieldsAreInvalid = nonNull(username)
                && nonNull(email)
                && nonNull(fullName)
                && nonNull(countryId)
                && nonNull(age)
                && nonNull(password)
                && nonNull(confirmPassword);
        if (allFieldsAreInvalid) {
            assertEquals("Not allowed symbols", responseJson.get("username").asText());
            assertEquals(
                    "must be a well-formed email address",
                    responseJson.get("email").asText());
            assertEquals("Not allowed symbols", responseJson.get("fullName").asText());
            assertEquals("must not be null", responseJson.get("countryId").asText());
            assertEquals(
                    "Not allowed age, should be in 5-200",
                    responseJson.get("age").asText());
            assertEquals("Not allowed symbols", responseJson.get("password").asText());
            assertEquals(
                    "Not allowed symbols", responseJson.get("confirmPassword").asText());
        } else {
            if (nonNull(username))
                assertEquals("Not allowed symbols", responseJson.get("username").asText());
            if (nonNull(email))
                assertEquals(
                        "must be a well-formed email address",
                        responseJson.get("email").asText());
            if (nonNull(fullName))
                assertEquals("Not allowed symbols", responseJson.get("fullName").asText());
            if (nonNull(countryId))
                assertEquals("must not be null", responseJson.get("countryId").asText());
            if (nonNull(age))
                assertEquals(
                        "Not allowed age, should be in 5-200",
                        responseJson.get("age").asText());
            if (nonNull(password) && nonNull(confirmPassword) && password.equals(confirmPassword)) {
                assertEquals("Not allowed symbols", responseJson.get("password").asText());
                assertEquals(
                        "Not allowed symbols",
                        responseJson.get("confirmPassword").asText());
            }
            if (nonNull(password) && nonNull(confirmPassword) && !password.equals(confirmPassword))
                assertEquals(
                        "Passwords must match",
                        responseJson.get("confirmPassword,password").asText());
        }
    }

    static Stream<Arguments> updateUserMultipleInvalidInputs() {
        return Stream.of(
                Arguments.of("Username%^&", null, null, null, null, null, null),
                Arguments.of(null, "email()@email.com", null, null, null, null, null),
                Arguments.of(null, null, "Full name !@#", null, null, null, null),
                Arguments.of(null, null, null, -1, null, null, null),
                Arguments.of(null, null, null, null, 1, null, null),
                Arguments.of(null, null, null, null, null, "Password 1", "Password 1"),
                Arguments.of(null, null, null, null, null, "Password_1", "Password_2"),
                Arguments.of("Username%^&", "email()@email.com", "Full name !@#", -1, 1, "Password 1", "Password 1"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleInvalidSizeInputs")
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnErrorMessageAnd400_whenInvalidSizeInputGiven(
            String username, String email, String fullName, String password, String confirmPassword) throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        userRequestDtoWrapper.setFieldValue("countryId", userWrapper.getCountryId());
        if (nonNull(username)) userRequestDtoWrapper.setFieldValue("username", username);
        if (nonNull(email)) userRequestDtoWrapper.setFieldValue("email", email);
        if (nonNull(fullName)) userRequestDtoWrapper.setFieldValue("fullName", fullName);
        if (nonNull(password) && nonNull(confirmPassword)) {
            userRequestDtoWrapper.setFieldValue("password", password);
            userRequestDtoWrapper.setFieldValue("confirmPassword", confirmPassword);
        }

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL, userWrapper.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);

        boolean allFieldsAreInvalid = nonNull(username)
                && nonNull(email)
                && nonNull(fullName)
                && nonNull(password)
                && nonNull(confirmPassword);
        if (allFieldsAreInvalid) {
            assertEquals(
                    "size must be between 6 and 20",
                    responseJson.get("username").asText());
            assertEquals(
                    "size must be between 6 and 64", responseJson.get("email").asText());
            assertEquals(
                    "size must be between 4 and 64",
                    responseJson.get("fullName").asText());
            assertEquals(
                    "size must be between 10 and 64",
                    responseJson.get("password").asText());
            assertEquals(
                    "size must be between 10 and 64",
                    responseJson.get("confirmPassword").asText());
        } else {
            if (nonNull(username))
                assertEquals(
                        "size must be between 6 and 20",
                        responseJson.get("username").asText());
            if (nonNull(email))
                assertEquals(
                        "size must be between 6 and 64",
                        responseJson.get("email").asText());
            if (nonNull(fullName))
                assertEquals(
                        "size must be between 4 and 64",
                        responseJson.get("fullName").asText());
            if (nonNull(password) && nonNull(confirmPassword)) {
                assertEquals(
                        "size must be between 10 and 64",
                        responseJson.get("password").asText());
                assertEquals(
                        "size must be between 10 and 64",
                        responseJson.get("confirmPassword").asText());
            }
        }
    }

    static Stream<Arguments> updateUserMultipleInvalidSizeInputs() {
        return Stream.of(
                Arguments.of("Usern", null, null, null, null),
                Arguments.of("UsernameUsernameUsername", null, null, null, null),
                Arguments.of(
                        null,
                        "longemail_longemail_longemail_longemail_longemail_longemail@email.com",
                        null,
                        null,
                        null),
                Arguments.of(null, null, "Nam", null, null),
                Arguments.of(
                        null,
                        null,
                        "Long Full Name Long Full Name Long Full Name Long Full Name Long Full Name",
                        null,
                        null),
                Arguments.of(null, null, null, "Short", "Short"),
                Arguments.of(
                        null,
                        null,
                        null,
                        "Long_Password_Long_Password_Long_Password_Long_Password_Long_Password",
                        "Long_Password_Long_Password_Long_Password_Long_Password_Long_Password"));
    }

    @Test
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatchOccurred() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();

        userRequestDtoWrapper.setFieldValue("countryId", userWrapper.getCountryId());
        userRequestDtoWrapper.setFieldValue("username", "New-username");
        long wrongUserId = userWrapper.getUserId() + 1;

        // When
        mockMvc.perform(patch(URL, wrongUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(
            username = "Username-1",
            password = "Password-1",
            authorities = {"ROLE_USER"})
    void updateUserTest_shouldReturnErrorMessageAnd404_whenCountryNotFound() throws Exception {
        // Given
        UserJpaTestBuilder.UserWrapper userWrapper = userJpaTestBuilder.getWrapper();
        userWrapper
                .setUserIdOrSeed(1)
                .setUserRole()
                .setRoleId(1)
                .setCountryIdOrSeed(1)
                .buildUser();

        UserDtoTestBuilder.UserDtoWrapper<UserUpdateRequestDto> userRequestDtoWrapper =
                userDtoTestBuilder.getWrapper(UserUpdateRequestDto.class);
        userRequestDtoWrapper.buildUserUpdateRequestDto();
        long wrongCountryId = userWrapper.getCountryId() + 1;
        userRequestDtoWrapper.setFieldValue("countryId", wrongCountryId);

        // When
        mockMvc.perform(patch(URL, userWrapper.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDtoWrapper.getDto())))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is(ErrorMessage.COUNTRY_NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andDo(print());
    }
}
