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
import healthy.lifestyle.backend.workout.dto.CreateHttpRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.Set;
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
class HttpRefControllerTest {
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

    @Autowired
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @BeforeEach
    void beforeEach() {
        dataHelper.deleteAll();
    }

    private static final String URL = "/api/v1/workouts/httpRefs";

    @Test
    void postgresqlContainerTest() {
        assertThat(postgresqlContainer.isRunning()).isTrue();
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnDefaultAndCustomHttpRefsAnd200Ok_whenUserAuthorized() throws Exception {
        // Given
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, false);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);

        Exercise exercise1 = dataHelper.createExercise(1, true, false, Set.of(bodyPart), Set.of(httpRef2));

        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;
        User user1 = dataHelper.createUser("one", role, country, Set.of(exercise1), age);

        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        Exercise exercise2 = dataHelper.createExercise(2, true, false, Set.of(bodyPart), Set.of(httpRef1, httpRef3));
        User user2 = dataHelper.createUser("two", role, country, Set.of(exercise2), age);

        Exercise exercise3 = dataHelper.createExercise(3, false, false, Set.of(bodyPart), Set.of(httpRef1));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(httpRef1.getId(), responseDto.get(0).getId());
        assertEquals(httpRef1.getName(), responseDto.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDto.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDto.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDto.get(1).getId());
        assertEquals(httpRef2.getName(), responseDto.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDto.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDto.get(1).getDescription());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnDefaultHttpRefsAnd200Ok_whenUserNotAuthorized() throws Exception {
        // Given
        BodyPart bodyPart = dataHelper.createBodyPart(1);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, false);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, false);

        Exercise exercise1 = dataHelper.createExercise(1, true, false, Set.of(bodyPart), Set.of(httpRef2));

        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;
        User user1 = dataHelper.createUser("one", role, country, Set.of(exercise1), age);

        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        Exercise exercise2 = dataHelper.createExercise(2, true, false, Set.of(bodyPart), Set.of(httpRef1, httpRef3));

        User user2 = dataHelper.createUser("two", role, country, Set.of(exercise2), age);

        Exercise exercise3 = dataHelper.createExercise(3, false, false, Set.of(bodyPart), Set.of(httpRef1));

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertEquals(httpRef1.getId(), responseDto.get(0).getId());
        assertEquals(httpRef1.getName(), responseDto.get(0).getName());
        assertEquals(httpRef1.getRef(), responseDto.get(0).getRef());
        assertEquals(httpRef1.getDescription(), responseDto.get(0).getDescription());

        assertEquals(httpRef2.getId(), responseDto.get(1).getId());
        assertEquals(httpRef2.getName(), responseDto.get(1).getName());
        assertEquals(httpRef2.getRef(), responseDto.get(1).getRef());
        assertEquals(httpRef2.getDescription(), responseDto.get(1).getDescription());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getHttpRefsTest_shouldReturnErrorMessageAnd500InternalServerError_whenNoHttpRefs() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        Integer age = 20;
        User user = dataHelper.createUser("one", role, country, null, age);

        // When
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Server error")))
                .andDo(print());
    }

    @Test
    void getHttpRefsTest_shouldReturn401Unauthorized_whenUserNotAuthorized() throws Exception {
        // When
        mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void getDefaultHttpRefsTest_shouldReturnDefaultHttpRefsAnd200Ok() throws Exception {
        // Given
        List<HttpRef> defaultHttpRefs = IntStream.rangeClosed(1, 5)
                .mapToObj(id -> dataHelper.createHttpRef(id, false))
                .toList();

        // When
        String URL_POSTFIX = "/default";
        MvcResult mvcResult = mockMvc.perform(get(URL + URL_POSTFIX).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(defaultHttpRefs.size(), responseDto.size());
        assertThat(defaultHttpRefs)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(responseDto);
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnHttpRefResponseDtoAnd201Created() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(createHttpRequestDto.getName())))
                .andExpect(jsonPath("$.description", is(createHttpRequestDto.getDescription())))
                .andExpect(jsonPath("$.ref", is(createHttpRequestDto.getRef())))
                .andExpect(jsonPath("$.custom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnUserNotFoundAnd400BadRequest_whenInvalidUserIdProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("two", role, country, null, 20);
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnAlreadyExistsAnd400BadRequest_whenDuplicatedNameProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);
        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.ALREADY_EXISTS.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageAnd400BadRequest_whenTooShortNameProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        String wrongValue = "abc";
        createHttpRequestDto.setName(wrongValue);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Size should be from 5 to 255 characters long")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageAnd400BadRequest_whenInvalidNameProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        String wrongValue = "abc@def#";
        createHttpRequestDto.setName(wrongValue);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageAnd400BadRequest_whenInvalidDescriptionProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        String wrongValue = "abc@def#";
        createHttpRequestDto.setDescription(wrongValue);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageAnd400BadRequest_whenInvalidRefProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        CreateHttpRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
        String wrongValue = "abc@def#";
        createHttpRequestDto.setRef(wrongValue);

        // When
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ref", is("Invalid format, should start with http")))
                .andDo(print());
    }
}
