package healthy.lifestyle.backend.users.controller;

import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.exception.ExceptionDto;
import healthy.lifestyle.backend.users.dto.CountryResponseDto;
import healthy.lifestyle.backend.users.dto.UserResponseDto;
import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.model.Workout;
import java.util.List;
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
@Import(BeanConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DbUtil dbUtil;

    @Autowired
    DtoUtil dtoUtil;

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse(ContainerConfig.POSTGRES));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    @BeforeEach
    void beforeEach() {
        dbUtil.deleteAll();
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getUserDetailsTest_shouldReturnUserDtoWith200_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.USERS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        UserResponseDto responseDto = objectMapper.readValue(responseContent, new TypeReference<UserResponseDto>() {});
        assertEquals(user.getId(), responseDto.getId());
        assertEquals(user.getUsername(), responseDto.getUsername());
        assertEquals(user.getEmail(), responseDto.getEmail());
        assertEquals(user.getFullName(), responseDto.getFullName());
        assertEquals(user.getCountry().getId(), responseDto.getCountryId());
        assertEquals(user.getAge(), responseDto.getAge());
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputs")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUserTest_shouldReturnUpdatedUserDtoWith200_whenValidRequest(
            String username,
            String email,
            String fullName,
            String countryName,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        Country newCountry = dbUtil.createCountry(2);
        String initialUsername = user.getUsername();
        String initialEmail = user.getEmail();
        String initialFullName = user.getFullName();
        int initialAge = user.getAge();
        String initialPassword = "Password-1";

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (!"Country 2".equals(countryName)) requestDto.setCountryId(newCountry.getId());
        else requestDto.setCountryId(user.getCountry().getId());
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setAge(age);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        UserResponseDto responseDto = objectMapper.readValue(responseContent, UserResponseDto.class);

        if (nonNull(username)) assertEquals(requestDto.getUsername(), responseDto.getUsername());
        else assertEquals(initialUsername, responseDto.getUsername());

        if (nonNull(email)) assertEquals(requestDto.getEmail(), responseDto.getEmail());
        else assertEquals(initialEmail, responseDto.getEmail());

        if (nonNull(fullName)) assertEquals(requestDto.getFullName(), responseDto.getFullName());
        else assertEquals(initialFullName, responseDto.getFullName());

        assertEquals(requestDto.getCountryId(), responseDto.getCountryId());

        if (nonNull(age)) assertEquals(requestDto.getAge(), responseDto.getAge());
        else assertEquals(initialAge, responseDto.getAge());

        if (nonNull(password) && nonNull(confirmPassword)) {
            User updatedUser = dbUtil.getUserById(user.getId());
            assertTrue(passwordEncoder.matches(requestDto.getPassword(), updatedUser.getPassword()));
        } else assertTrue(passwordEncoder.matches(initialPassword, user.getPassword()));
    }

    static Stream<Arguments> updateUserMultipleValidInputs() {
        return Stream.of(
                Arguments.of("new-username", null, null, null, null, null, null),
                Arguments.of(null, "new-email@email.com", null, null, null, null, null),
                Arguments.of(null, null, "New full name", null, null, null, null),
                Arguments.of(null, null, null, "Country 2", null, null, null),
                Arguments.of(null, null, null, null, 100, null, null),
                Arguments.of(null, null, null, null, null, "new-password", "new-password"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleValidInputsButNotDifferent")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUserTest_shouldReturnErrorMessageWith400_whenValidButNotDifferentInput(
            String username, String email, String fullName, Integer age, String password, String confirmPassword)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (nonNull(age)) {
            user.setAge(age);
            dbUtil.saveUserChanges(user);
            requestDto.setAge(age);
        }
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

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

    static Stream<Arguments> updateUserMultipleValidInputsButNotDifferent() {
        return Stream.of(
                Arguments.of("Username-1", null, null, null, null, null),
                Arguments.of(null, "email-1@email.com", null, null, null, null),
                Arguments.of(null, null, "Full Name One", null, null, null),
                Arguments.of(null, null, null, 20, null, null),
                Arguments.of(null, null, null, null, "Password-1", "Password-1"),
                Arguments.of("Username-1", "email-1@email.com", "Full Name One", 20, "Password-1", "Password-1"));
    }

    @ParameterizedTest
    @MethodSource("updateUserMultipleInvalidInputs")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUserTest_shouldReturnErrorMessageWith400_whenInvalidRequest(
            String username,
            String email,
            String fullName,
            Integer countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (nonNull(countryId)) requestDto.setCountryId((long) countryId);
        else requestDto.setCountryId(user.getCountry().getId());
        if (nonNull(age)) requestDto.setAge(age);
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

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
            assertEquals(
                    "must be greater than or equal to 0",
                    responseJson.get("countryId").asText());
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
                assertEquals(
                        "must be greater than or equal to 0",
                        responseJson.get("countryId").asText());
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
    @MethodSource("updateUserMultipleInvalidInputSize")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUserTest_shouldReturnErrorMessageWith400_whenInvalidInputSize(
            String username, String email, String fullName, String password, String confirmPassword) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setUsername(username);
        requestDto.setEmail(email);
        requestDto.setFullName(fullName);
        requestDto.setPassword(password);
        requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

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

    static Stream<Arguments> updateUserMultipleInvalidInputSize() {
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
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUserTest_shouldReturnErrorMessageWith400_whenUserResourceMismatch() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setUsername("New-username");
        long wrongUserId = user.getId() + 1;

        // When
        mockMvc.perform(patch(URL.USER_ID, wrongUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

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
    void updateUserTest_shouldReturnErrorMessageWith404_whenCountryNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername("New-username");
        long wrongCountryId = user.getCountry().getId() + 1;
        requestDto.setCountryId(wrongCountryId);

        // When
        mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is(ErrorMessage.COUNTRY_NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.INTERNAL_SERVER_ERROR.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteUserTest_shouldReturnVoid204_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        BodyPart bodyPart1 = dbUtil.createBodyPart(1);
        BodyPart bodyPart2 = dbUtil.createBodyPart(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Exercise customExercise1 = dbUtil.createCustomExercise(
                1, true, List.of(bodyPart1), List.of(customHttpRef1, defaultHttpRef1), user);
        Exercise customExercise2 = dbUtil.createCustomExercise(
                2, true, List.of(bodyPart2), List.of(customHttpRef2, defaultHttpRef2), user);
        Exercise defaultExercise1 = dbUtil.createDefaultExercise(3, true, List.of(bodyPart1), List.of(defaultHttpRef1));
        Exercise defaultExercise2 = dbUtil.createDefaultExercise(4, true, List.of(bodyPart2), List.of(defaultHttpRef2));

        Workout customWorkout1 = dbUtil.createCustomWorkout(1, List.of(customExercise1, defaultExercise1), user);
        Workout customWorkout2 = dbUtil.createCustomWorkout(2, List.of(customExercise2, defaultExercise2), user);
        Workout defaultWorkout1 = dbUtil.createDefaultWorkout(1, List.of(defaultExercise1));
        Workout defaultWorkout2 = dbUtil.createDefaultWorkout(2, List.of(defaultExercise2));

        long userId = user.getId();
        List<Long> customHttpRefsIds = List.of(customHttpRef1.getId(), customHttpRef2.getId());
        List<Long> defaultHttpRefsIds = List.of(defaultHttpRef1.getId(), defaultHttpRef2.getId());
        List<Long> customExercisesIds = List.of(customExercise1.getId(), customExercise2.getId());
        List<Long> defaultExercisesIds = List.of(defaultExercise1.getId(), defaultExercise2.getId());
        List<Long> customWorkoutIds = List.of(customWorkout1.getId(), customWorkout2.getId());
        List<Long> defaultWorkoutIds = List.of(defaultWorkout1.getId(), defaultWorkout2.getId());

        // When
        String REQUEST_URL = URL.USER_ID;
        mockMvc.perform(delete(REQUEST_URL, user.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        assertFalse(dbUtil.userExistsById(userId));
        assertFalse(dbUtil.httpRefsExistByIds(customHttpRefsIds));
        assertFalse(dbUtil.exercisesExistByIds(customExercisesIds));
        assertFalse(dbUtil.workoutsExistByIds(customWorkoutIds));
        assertTrue(dbUtil.httpRefsExistByIds(defaultHttpRefsIds));
        assertTrue(dbUtil.exercisesExistByIds(defaultExercisesIds));
        assertTrue(dbUtil.workoutsExistByIds(defaultWorkoutIds));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteUserTest_shouldReturnErrorMessageWith400_whenUserResourceMismatch() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long wrongUserId = user.getId() + 1;

        // When
        String REQUEST_URL = URL.USER_ID;
        mockMvc.perform(delete(REQUEST_URL, wrongUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user.getId())))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    void getCountriesTest_shouldReturnCountriesWith200_whenValidRequest() throws Exception {
        // Given
        Country country1 = dbUtil.createCountry(1);
        Country country2 = dbUtil.createCountry(2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.COUNTRIES).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<CountryResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<CountryResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(country1.getId(), responseDto.get(0).getId());
        assertEquals(country1.getName(), responseDto.get(0).getName());
        assertEquals(country2.getId(), responseDto.get(1).getId());
        assertEquals(country2.getName(), responseDto.get(1).getName());
    }

    @Test
    void getCountriesTest_shouldReturnErrorMessageWith500_whenNoCountries() throws Exception {
        // When
        mockMvc.perform(get(URL.COUNTRIES).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }
}
