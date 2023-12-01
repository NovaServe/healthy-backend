package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.data.DataConfiguration;
import healthy.lifestyle.backend.data.DataHelper;
import healthy.lifestyle.backend.data.DataUtil;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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
class ExerciseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    DataHelper dataHelper;

    @Autowired
    DataUtil dataUtil;

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Container
    static PostgreSQLContainer<?> postgresqlContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:12.15"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    private static final String URL = "/api/v1/workouts/exercises";

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnExerciseDtoAnd201Created() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        ExerciseCreateRequestDto createExerciseRequestDto = dataUtil.createExerciseRequestDto(
                1, false, new Long[] {bodyParts.get(0).getId(), bodyParts.get(1).getId()}, new Long[] {
                    httpRefs.get(0).getId(), httpRefs.get(1).getId()
                });

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExerciseRequestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(createExerciseRequestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(createExerciseRequestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto exerciseResponseDto = objectMapper.readValue(responseContent, ExerciseResponseDto.class);

        assertEquals(
                createExerciseRequestDto.getBodyParts().size(),
                exerciseResponseDto.getBodyParts().size());
        assertEquals(
                createExerciseRequestDto.getHttpRefs().size(),
                exerciseResponseDto.getHttpRefs().size());

        assertThat(bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseResponseDto.getBodyParts());

        assertThat(httpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(exerciseResponseDto.getHttpRefs());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnExerciseDtoAnd201Created_whenNoHttpRefsProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        ExerciseCreateRequestDto createExerciseRequestDto = dataUtil.createExerciseRequestDto(
                1, false, new Long[] {bodyParts.get(0).getId(), bodyParts.get(1).getId()}, new Long[] {});

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExerciseRequestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(createExerciseRequestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(createExerciseRequestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto exerciseResponseDto = objectMapper.readValue(responseContent, ExerciseResponseDto.class);

        assertEquals(
                createExerciseRequestDto.getBodyParts().size(),
                exerciseResponseDto.getBodyParts().size());
        assertTrue(createExerciseRequestDto.getHttpRefs().isEmpty());

        assertThat(bodyParts)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseResponseDto.getBodyParts());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnErrorMessageAnd400BadRequest_whenNoBodyPartsProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        ExerciseCreateRequestDto createExerciseRequestDto =
                dataUtil.createExerciseRequestDto(1, false, new Long[] {}, new Long[] {
                    httpRefs.get(0).getId(), httpRefs.get(1).getId()
                });

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExerciseRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid nested object")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnErrorMessageAnd400BadRequest_whenWrongBodyPartIdsProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        ExerciseCreateRequestDto createExerciseRequestDto =
                dataUtil.createExerciseRequestDto(1, false, new Long[] {1000L}, new Long[] {1111L});

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExerciseRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid nested object")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnErrorMessageAnd400BadRequest_whenWrongHttpRefIdsProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        ExerciseCreateRequestDto createExerciseRequestDto =
                dataUtil.createExerciseRequestDto(1, false, new Long[] {1000L}, new Long[] {1111L});

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createExerciseRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid nested object")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomExercisesTest_shouldReturnListOfExercisesAnd200Ok() throws Exception {
        // Given
        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();
        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 3)
                .mapToObj(id ->
                        dataHelper.createExercise(id, false, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        List<Exercise> customExercises = IntStream.rangeClosed(4, 6)
                .mapToObj(id ->
                        dataHelper.createExercise(id, true, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        List<Exercise> testUserCustomExercises = List.of(customExercises.get(0), customExercises.get(1));

        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;

        // Test user with 2 default and 2 custom exercises
        User testUser = dataHelper.createUser(
                "one",
                role,
                country,
                Set.of(
                        defaultExercises.get(0),
                        defaultExercises.get(1),
                        testUserCustomExercises.get(0),
                        testUserCustomExercises.get(1)),
                age);

        User otherUser = dataHelper.createUser(
                "two", role, country, Set.of(defaultExercises.get(2), customExercises.get(2)), age);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<ExerciseResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<ExerciseResponseDto>>() {});

        assertEquals(2, responseDto.size());

        assertThat(testUserCustomExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto);

        IntStream.range(0, testUserCustomExercises.size()).forEach(id -> {
            List<BodyPart> bodyParts_ = testUserCustomExercises.get(id).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();

            assertThat(bodyParts_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(responseDto.get(id).getBodyParts());

            List<HttpRef> httpRefs_ = testUserCustomExercises.get(id).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();

            assertThat(httpRefs_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(responseDto.get(id).getHttpRefs());
        });
    }

    @Test
    void getDefaultExercisesTest_shouldReturnListOfExercisesAnd200Ok() throws Exception {
        // Given
        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();
        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 3)
                .mapToObj(id ->
                        dataHelper.createExercise(id, false, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        List<Exercise> customExercises = IntStream.rangeClosed(4, 6)
                .mapToObj(id ->
                        dataHelper.createExercise(id, true, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        // When
        String postfix = "/default";
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<ExerciseResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<ExerciseResponseDto>>() {});

        assertEquals(3, responseDto.size());

        assertThat(defaultExercises)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto);

        IntStream.range(0, defaultExercises.size()).forEach(id -> {
            List<BodyPart> bodyParts_ = defaultExercises.get(id).getBodyParts().stream()
                    .sorted(Comparator.comparingLong(BodyPart::getId))
                    .toList();

            assertThat(bodyParts_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(responseDto.get(id).getBodyParts());

            List<HttpRef> httpRefs_ = defaultExercises.get(id).getHttpRefs().stream()
                    .sorted(Comparator.comparingLong(HttpRef::getId))
                    .toList();

            assertThat(httpRefs_)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                    .isEqualTo(responseDto.get(id).getHttpRefs());
        });
    }

    @Test
    void getExerciseByIdTest_ShouldReturnDefaultExerciseAnd200_whenDefaultExerciseRequested() throws Exception {
        // Given
        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();
        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<Exercise> exercises = IntStream.rangeClosed(1, 5)
                .mapToObj(id ->
                        dataHelper.createExercise(id, false, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        int exerciseListId = 0;
        long exerciseId = exercises.get(exerciseListId).getId();
        String REQUEST_URL = URL + "/default/{exerciseId}";

        // When
        MvcResult mvcResult = mockMvc.perform(get(REQUEST_URL, exerciseId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<ExerciseResponseDto>() {});

        assertThat(exercises.get(exerciseListId))
                .usingRecursiveComparison()
                .ignoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto);

        List<BodyPart> bodyParts_ = exercises.get(exerciseListId).getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(bodyParts_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        List<HttpRef> HttpRefs_ = exercises.get(exerciseListId).getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(HttpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(responseDto.getHttpRefs());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getExerciseByIdTest_ShouldReturnCustomExerciseAnd200_whenCustomExerciseRequested() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();
        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<Exercise> exercises = IntStream.rangeClosed(1, 2)
                .mapToObj(id ->
                        dataHelper.createExercise(id, true, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        int exerciseListId = 0;
        dataHelper.userAddExercises(user, new HashSet<>() {
            {
                add(exercises.get(exerciseListId));
            }
        });
        long exerciseId = exercises.get(exerciseListId).getId();
        String REQUEST_URL = URL + "/{exerciseId}";

        // When
        MvcResult mvcResult = mockMvc.perform(get(REQUEST_URL, exerciseId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<ExerciseResponseDto>() {});

        assertThat(exercises.get(exerciseListId))
                .usingRecursiveComparison()
                .ignoringFields("bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto);

        List<BodyPart> bodyPartsSorted = exercises.get(exerciseListId).getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(bodyPartsSorted)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        List<HttpRef> HttpRefsSorted = exercises.get(exerciseListId).getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(HttpRefsSorted)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(responseDto.getHttpRefs());
    }

    @Test
    void getExerciseByIdTest_ShouldReturnErrorMessageNotFoundAnd404_whenDefaultExerciseNotFound() throws Exception {
        // Given
        long wrongExerciseId = 1000L;
        String REQUEST_URL = URL + "/default/{exerciseId}";

        // When
        mockMvc.perform(get(REQUEST_URL, wrongExerciseId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getExerciseByIdTest_ShouldReturnErrorMessageNotFoundAnd404_whenCustomExerciseNotFound() throws Exception {
        // Given
        long wrongExerciseId = 1000L;
        String REQUEST_URL = URL + "/{exerciseId}";

        // When
        mockMvc.perform(get(REQUEST_URL, wrongExerciseId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.NOT_FOUND.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getExerciseByIdTest_ShouldReturnErrorUserResourceMismatchAnd400_whenRequestedExerciseBelongsToAnotherUser()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);

        Exercise exercise1 = dataHelper.createExercise(1, true, false, null, null);
        User user1 = dataHelper.createUser("one", role, country, Set.of(exercise1), 20);

        Exercise exercise2 = dataHelper.createExercise(2, true, false, null, null);
        User user2 = dataHelper.createUser("two", role, country, Set.of(exercise2), 20);

        String REQUEST_URL = URL + "/{exerciseId}";

        // When
        mockMvc.perform(get(REQUEST_URL, exercise2.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }
}
