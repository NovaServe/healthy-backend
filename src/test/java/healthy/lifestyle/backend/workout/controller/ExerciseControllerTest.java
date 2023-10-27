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
        User user = dataHelper.createUser("one", role, country, null);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        CreateExerciseRequestDto createExerciseRequestDto = dataUtil.createExerciseRequestDto(
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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(exerciseResponseDto.getHttpRefs());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomExerciseTest_shouldReturnExerciseDtoAnd201Created_whenNoHttpRefsProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        CreateExerciseRequestDto createExerciseRequestDto = dataUtil.createExerciseRequestDto(
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
        User user = dataHelper.createUser("one", role, country, null);

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        CreateExerciseRequestDto createExerciseRequestDto =
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
        User user = dataHelper.createUser("one", role, country, null);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        CreateExerciseRequestDto createExerciseRequestDto =
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
        User user = dataHelper.createUser("one", role, country, null);

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        CreateExerciseRequestDto createExerciseRequestDto =
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
        // Test user with 2 default and 2 custom exercises
        User testUser = dataHelper.createUser(
                "one",
                role,
                country,
                Set.of(
                        defaultExercises.get(0),
                        defaultExercises.get(1),
                        testUserCustomExercises.get(0),
                        testUserCustomExercises.get(1)));

        User otherUser =
                dataHelper.createUser("two", role, country, Set.of(defaultExercises.get(2), customExercises.get(2)));

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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("isCustom", "bodyParts", "httpRefs", "users")
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
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
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
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("isCustom", "bodyParts", "httpRefs", "users")
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
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                    .isEqualTo(responseDto.get(id).getHttpRefs());
        });
    }

    @Test
    void getDefaultExerciseTest_ShouldReturnExerciseAnd200Ok() throws Exception {
        // Given
        int exercise_id = 0;

        List<BodyPart> bodyParts = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createBodyPart(id))
                .toList();

        List<HttpRef> httpRefs = IntStream.rangeClosed(1, 2)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        List<Exercise> defaultExercises = IntStream.rangeClosed(1, 5)
                .mapToObj(id ->
                        dataHelper.createExercise(id, false, false, new HashSet<>(bodyParts), new HashSet<>(httpRefs)))
                .toList();

        long id = defaultExercises.get(exercise_id).getId();

        // When
        String postfix = String.format("/default/%d", id);
        MvcResult mvcResult = mockMvc.perform(get(URL + postfix).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        ExerciseResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<ExerciseResponseDto>() {});

        assertThat(defaultExercises.get(exercise_id))
                .usingRecursiveComparison()
                .ignoringFields("isCustom", "bodyParts", "httpRefs", "users")
                .isEqualTo(responseDto);

        List<BodyPart> bodyParts_ = defaultExercises.get(exercise_id).getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPart::getId))
                .toList();
        assertThat(bodyParts_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getBodyParts());

        List<HttpRef> HttpRefs_ = defaultExercises.get(exercise_id).getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRef::getId))
                .toList();
        assertThat(HttpRefs_)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises")
                .isEqualTo(responseDto.getHttpRefs());
    }
}
