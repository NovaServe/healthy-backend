package healthy.lifestyle.backend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.workout.model.BodyPart;
import healthy.lifestyle.backend.activity.workout.model.Exercise;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.activity.workout.model.Workout;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.dto.*;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.validation.UserValidationMessage;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    FirebaseMessaging firebaseMessaging;

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

    @ParameterizedTest
    @MethodSource("signupAgeValues")
    void signup_shouldReturnVoidWith201_whenValidFields(Integer age) throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone();
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId(), timezone.getId());
        if (age != null) {
            requestDto.setAge(age);
        }

        // When
        mockMvc.perform(post(URL.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());
    }

    static Stream<Arguments> signupAgeValues() {
        return Stream.of(Arguments.of(20), Arguments.of((Object) null));
    }

    @Test
    void signup_shouldReturnValidationMessageWith400_whenInvalidFields() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDtoEmpty();
        requestDto.setUsername("username!");
        requestDto.setEmail("email@@email.com");
        requestDto.setFullName("Name 1");
        requestDto.setCountryId(-1L);
        requestDto.setAge(15);
        requestDto.setPassword("password space");
        requestDto.setConfirmPassword("password space");

        String usernameMessage = "Username can include lower and upper-case letters, digits, and symbols: . - _";
        String emailMessage = "Email can contain lower and upper-case letters, digits, and symbols: . - _";
        String fullNameMessage = "Full name can include lower and upper-case letters, and spaces";
        String idMessage = "Id must be equal or greater than 0";
        String ageMessage = "Age must be between 16 and 120";
        String passwordMessage =
                "Password can include lower and upper-case letters, digits, and symbols: . , - _ < > : ; ! ? # $ % ^ & * ( ) + =";

        // When
        mockMvc.perform(post(URL.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(usernameMessage)))
                .andExpect(jsonPath("$.email", is(emailMessage)))
                .andExpect(jsonPath("$.fullName", is(fullNameMessage)))
                .andExpect(jsonPath("$.countryId", is(idMessage)))
                .andExpect(jsonPath("$.age", is(ageMessage)))
                .andExpect(jsonPath("$.password", is(passwordMessage)))
                .andExpect(jsonPath("$.confirmPassword", is(passwordMessage)))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("signupNullOrBlankOrInvalidLengthFields")
    void signup_shouldReturnValidationMessageWith400_whenNullOrBlankOrInvalidLengthFields(
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
        if (username != null) requestDto.setUsername(username);
        if (email != null) requestDto.setEmail(email);
        if (fullName != null) requestDto.setFullName(fullName);
        if (countryId != null) requestDto.setCountryId(countryId);
        if (age != null) requestDto.setAge(age);
        if (password != null) requestDto.setPassword(password);
        if (confirmPassword != null) requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(UserValidationMessage.USERNAME_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.email", is(UserValidationMessage.EMAIL_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.fullName", is(UserValidationMessage.FULL_NAME_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.countryId", is("Id must be equal or greater than 0")))
                .andExpect(jsonPath("$.password", is(UserValidationMessage.PASSWORD_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.confirmPassword", is(UserValidationMessage.PASSWORD_LENGTH_RANGE.getName())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("age");
        if (age == null) {
            assertTrue(contentNode.asText().isEmpty());
        } else {
            assertEquals("Age must be between 16 and 120", contentNode.asText());
        }
    }

    static Stream<Arguments> signupNullOrBlankOrInvalidLengthFields() {
        return Stream.of(
                // null
                Arguments.of(null, null, null, null, null, null, null),
                // blank
                Arguments.of("", "", "", null, null, "", ""),
                Arguments.of(" ", " ", " ", null, null, " ", " "),
                // invalid length
                Arguments.of("user", "email", "f", -1L, null, "pass", "pass"),
                Arguments.of("user", "email", "f", -1L, 15, "pass", "pass"),
                Arguments.of("user", "email", "f", -1L, 121, "pass", "pass"));
    }

    @Test
    void signup_shouldReturnValidationMessageWith400_whenPasswordsMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDtoEmpty();
        requestDto.setPassword("password-1");
        requestDto.setConfirmPassword("password-2");

        // When
        mockMvc.perform(post(URL.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password,confirmPassword", is("Passwords mismatch")))
                .andDo(print());
    }

    @Test
    void signup_shouldReturnErrorMessageWith400_whenUserAlreadyExists() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User alreayExistentUser = dbUtil.createUser(1, role, country, timezone);
        SignupRequestDto requestDto = dtoUtil.signupRequestDto(1, country.getId(), timezone.getId());

        // When
        mockMvc.perform(post(URL.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.ALREADY_EXISTS.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getUserDetails_shouldReturnDtoWith200_whenValidRequest() throws Exception {
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
        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("countryId", "timezoneId")
                .isEqualTo(user);
        assertEquals(user.getCountry().getId(), responseDto.getCountryId());
    }

    @ParameterizedTest
    @MethodSource("updateUserValidFields")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnUpdatedDtoWith200_whenValidFields(
            String username,
            String email,
            String fullName,
            String countryName,
            String timezoneName,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        String initialUsername = user.getUsername();
        String initialEmail = user.getEmail();
        String initialFullName = user.getFullName();
        int initialAge = user.getAge();
        String initialPassword = "Password-1";
        Country newCountry = dbUtil.createCountry(2);
        Timezone newTimezone = dbUtil.createTimezone(2);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (countryName != null) requestDto.setCountryId(newCountry.getId());
        else requestDto.setCountryId(user.getCountry().getId());

        if (timezoneName != null) requestDto.setTimezoneId(newTimezone.getId());
        else requestDto.setTimezoneId(user.getTimezone().getId());

        if (username != null) requestDto.setUsername(username);
        if (email != null) requestDto.setEmail(email);
        if (fullName != null) requestDto.setFullName(fullName);
        if (age != null) requestDto.setAge(age);
        if (password != null) requestDto.setPassword(password);
        if (confirmPassword != null) requestDto.setConfirmPassword(confirmPassword);

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

        if (username != null) assertEquals(requestDto.getUsername(), responseDto.getUsername());
        else assertEquals(initialUsername, responseDto.getUsername());

        if (email != null) assertEquals(requestDto.getEmail(), responseDto.getEmail());
        else assertEquals(initialEmail, responseDto.getEmail());

        if (fullName != null) assertEquals(requestDto.getFullName(), responseDto.getFullName());
        else assertEquals(initialFullName, responseDto.getFullName());

        assertEquals(requestDto.getCountryId(), responseDto.getCountryId());

        if (age != null) assertEquals(requestDto.getAge(), responseDto.getAge());
        else assertEquals(initialAge, responseDto.getAge());

        if (password != null && confirmPassword != null) {
            User updatedUser = dbUtil.getUserById(user.getId());
            assertTrue(passwordEncoder.matches(requestDto.getPassword(), updatedUser.getPassword()));
        } else assertTrue(passwordEncoder.matches(initialPassword, user.getPassword()));
    }

    static Stream<Arguments> updateUserValidFields() {
        return Stream.of(
                Arguments.of("new-username", null, null, null, null, null, null, null),
                Arguments.of(null, "new-email@email.com", null, null, null, null, null, null),
                Arguments.of(null, null, "New full name", null, null, null, null, null),
                Arguments.of(null, null, null, "New Country", null, null, null, null),
                Arguments.of(null, null, null, null, "New Timezone", null, null, null),
                Arguments.of(null, null, null, null, null, 25, null, null),
                Arguments.of(null, null, null, null, null, null, "new-password", "new-password"));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith400_whenNotDifferentFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1, 20);
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername(user.getUsername());
        requestDto.setEmail(user.getEmail());
        requestDto.setFullName(user.getFullName());
        requestDto.setAge(user.getAge());
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setTimezoneId(user.getTimezone().getId());
        requestDto.setPassword("Password-1");
        requestDto.setConfirmPassword("Password-1");
        String errorMessage =
                ErrorMessage.FIELDS_VALUES_ARE_NOT_DIFFERENT.getName() + "username, email, fullName, age, password";

        // When
        mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(errorMessage)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith400_whenInvalidFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        SignupRequestDto requestDto = dtoUtil.signupRequestDtoEmpty();
        requestDto.setUsername("username!");
        requestDto.setEmail("email@@email.com");
        requestDto.setFullName("Name 1");
        requestDto.setCountryId(-1L);
        requestDto.setAge(15);
        requestDto.setPassword("password space");
        requestDto.setConfirmPassword("password space");

        String usernameMessage = "Username can include lower and upper-case letters, digits, and symbols: . - _";
        String emailMessage = "Email can contain lower and upper-case letters, digits, and symbols: . - _";
        String fullNameMessage = "Full name can include lower and upper-case letters, and spaces";
        String idMessage = "Id must be equal or greater than 0";
        String ageMessage = "Age must be between 16 and 120";
        String passwordMessage =
                "Password can include lower and upper-case letters, digits, and symbols: . , - _ < > : ; ! ? # $ % ^ & * ( ) + =";

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(usernameMessage)))
                .andExpect(jsonPath("$.email", is(emailMessage)))
                .andExpect(jsonPath("$.fullName", is(fullNameMessage)))
                .andExpect(jsonPath("$.countryId", is(idMessage)))
                .andExpect(jsonPath("$.age", is(ageMessage)))
                .andExpect(jsonPath("$.password", is(passwordMessage)))
                .andExpect(jsonPath("$.confirmPassword", is(passwordMessage)))
                .andDo(print())
                .andReturn();
    }

    @ParameterizedTest
    @MethodSource("updateUserBlankOrInvalidLengthFields")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith400_whenBlankOrInvalidLengthFields(
            String username,
            String email,
            String fullName,
            Long countryId,
            Integer age,
            String password,
            String confirmPassword)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        if (username != null) requestDto.setUsername(username);
        if (email != null) requestDto.setEmail(email);
        if (fullName != null) requestDto.setFullName(fullName);
        if (countryId != null) requestDto.setCountryId(countryId);
        if (age != null) requestDto.setAge(age);
        if (password != null) requestDto.setPassword(password);
        if (confirmPassword != null) requestDto.setConfirmPassword(confirmPassword);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username", is(UserValidationMessage.USERNAME_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.email", is(UserValidationMessage.EMAIL_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.fullName", is(UserValidationMessage.FULL_NAME_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.countryId", is("Id must be equal or greater than 0")))
                .andExpect(jsonPath("$.password", is(UserValidationMessage.PASSWORD_LENGTH_RANGE.getName())))
                .andExpect(jsonPath("$.confirmPassword", is(UserValidationMessage.PASSWORD_LENGTH_RANGE.getName())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("age");
        if (age == null) {
            assertTrue(contentNode.asText().isEmpty());
        } else {
            assertEquals("Age must be between 16 and 120", contentNode.asText());
        }
    }

    static Stream<Arguments> updateUserBlankOrInvalidLengthFields() {
        return Stream.of(
                // blank
                Arguments.of("", "", "", null, null, "", ""),
                Arguments.of(" ", " ", " ", null, null, " ", " "),
                // invalid length
                Arguments.of("user", "email", "f", -1L, null, "pass", "pass"),
                Arguments.of("user", "email", "f", -1L, 15, "pass", "pass"),
                Arguments.of("user", "email", "f", -1L, 121, "pass", "pass"));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith400_whenNoUpdatesRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user.getCountry().getId());
        requestDto.setTimezoneId(user.getTimezone().getId());

        // When
        mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NO_UPDATES_REQUEST.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith400_whenUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setCountryId(user1.getCountry().getId());
        requestDto.setTimezoneId(user1.getTimezone().getId());
        requestDto.setUsername("New-username");

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_REQUESTED_ANOTHER_USER_PROFILE, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.USER_ID, user2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateUser_shouldReturnErrorMessageWith404_whenCountryNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        UserUpdateRequestDto requestDto = dtoUtil.userUpdateRequestDtoEmpty();
        requestDto.setUsername("New-username");
        long nonExistentCountryId = 1000L;
        requestDto.setCountryId(nonExistentCountryId);
        requestDto.setTimezoneId(user.getTimezone().getId());
        ApiException expectedException =
                new ApiException(ErrorMessage.COUNTRY_NOT_FOUND, nonExistentCountryId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(patch(URL.USER_ID, user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteUser_shouldReturnVoid204_whenValidRequest() throws Exception {
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
        mockMvc.perform(delete(REQUEST_URL, user.getId()))

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
    void deleteUser_shouldReturnErrorMessageWith400_whenUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_REQUESTED_ANOTHER_USER_PROFILE, null, HttpStatus.BAD_REQUEST);

        // When
        String REQUEST_URL = URL.USER_ID;
        mockMvc.perform(delete(REQUEST_URL, user2.getId()))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    void getCountries_shouldReturnDtoListWith200_whenValidRequest() throws Exception {
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
    void getCountries_shouldReturnErrorMessageWith404_whenCountriesNotFound() throws Exception {
        // Given
        ApiException expectedException = new ApiException(ErrorMessage.NOT_FOUND, null, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.COUNTRIES).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    void getTimezones_shouldReturnDtoListWith200_whenValidRequest() throws Exception {
        // Given
        Timezone timezone1 = dbUtil.createTimezone(1);
        Timezone timezone2 = dbUtil.createTimezone(2);
        Timezone timezone3 = dbUtil.createTimezone(3);
        List<Timezone> timezones = List.of(timezone1, timezone2, timezone3);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.TIMEZONES).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<TimezoneResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<TimezoneResponseDto>>() {});

        assertEquals(timezones.size(), responseDto.size());
        assertThat(responseDto).usingRecursiveFieldByFieldElementComparator().isEqualTo(timezones);
    }
}
