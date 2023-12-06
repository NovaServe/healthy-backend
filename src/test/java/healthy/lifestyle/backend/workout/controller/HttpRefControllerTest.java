package healthy.lifestyle.backend.workout.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void getCustomHttpRefsTest_shouldReturnCustomHttpRefsAnd200_whenUserAuthorized() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        Country country1 = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country1, null, 20);
        HttpRef httpRef1 = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef1, user1);
        HttpRef httpRef2 = dataHelper.createHttpRef(2, true);
        dataHelper.httpRefAddUser(httpRef2, user1);

        Country country2 = dataHelper.createCountry(2);
        User user2 = dataHelper.createUser("two", role, country2, null, 20);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        dataHelper.httpRefAddUser(httpRef3, user2);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        dataHelper.httpRefAddUser(httpRef4, user2);

        HttpRef defaultHttpRef1 = dataHelper.createHttpRef(5, false);
        HttpRef defaultHttpRef2 = dataHelper.createHttpRef(6, false);

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

        assertThat(List.of(httpRef1, httpRef2))
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(responseDto);
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomHttpRefsTest_shouldReturnEmptyListAnd200_whenNoHttpRefsFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");

        Country country1 = dataHelper.createCountry(1);
        User user1 = dataHelper.createUser("one", role, country1, null, 20);

        Country country2 = dataHelper.createCountry(2);
        User user2 = dataHelper.createUser("two", role, country2, null, 20);
        HttpRef httpRef3 = dataHelper.createHttpRef(3, true);
        dataHelper.httpRefAddUser(httpRef3, user2);
        HttpRef httpRef4 = dataHelper.createHttpRef(4, true);
        dataHelper.httpRefAddUser(httpRef4, user2);

        HttpRef defaultHttpRef1 = dataHelper.createHttpRef(5, false);
        HttpRef defaultHttpRef2 = dataHelper.createHttpRef(6, false);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(0, responseDto.size());
    }

    @Test
    void getCustomHttpRefsTest_shouldReturn401_whenUserNotAuthorized() throws Exception {
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
    void createCustomHttpRefTest_shouldReturnHttpRefResponseDtoAnd201Created_whenValidDtoProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);

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
                .andExpect(jsonPath("$.isCustom", is(true)))
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
        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);

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

        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
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

        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
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

        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
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

        HttpRefCreateRequestDto createHttpRequestDto = dataUtil.createHttpRequestDto(1);
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

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnHttpRefResponseDtoAnd200_whenValidUpdateDtoProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(httpRef.getId().intValue())))
                .andExpect(jsonPath("$.name", is(requestDto.getUpdatedName())))
                .andExpect(jsonPath("$.description", is(requestDto.getUpdatedDescription())))
                .andExpect(jsonPath("$.ref", is(requestDto.getUpdatedRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnHttpRefResponseDtoAnd200_whenUpdateNameDtoProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);
        requestDto.setUpdatedDescription(null);
        requestDto.setUpdatedRef(null);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(httpRef.getId().intValue())))
                .andExpect(jsonPath("$.name", is(requestDto.getUpdatedName())))
                .andExpect(jsonPath("$.description", is(httpRef.getDescription())))
                .andExpect(jsonPath("$.ref", is(httpRef.getRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnHttpRefResponseDtoAnd200_whenUpdateDescAndRefDtoProvided()
            throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);
        requestDto.setUpdatedName(null);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(httpRef.getId().intValue())))
                .andExpect(jsonPath("$.name", is(httpRef.getName())))
                .andExpect(jsonPath("$.description", is(requestDto.getUpdatedDescription())))
                .andExpect(jsonPath("$.ref", is(requestDto.getUpdatedRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnNotFoundAnd400_whenHttpRefNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);

        long wrongHttpRefId = httpRef.getId() + 1;

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, wrongHttpRefId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenDefaultHttpRefUpdateRequested() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, false);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatch() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        User userMismatch = dataHelper.createUser("two", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, userMismatch);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenEmptyRequestDtoProvided() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        User userMismatch = dataHelper.createUser("two", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, userMismatch);

        HttpRefUpdateRequestDto requestDto = dataUtil.createUpdateHttpRefRequestDto(2);
        requestDto.setUpdatedName(null);
        requestDto.setUpdatedDescription(null);
        requestDto.setUpdatedRef(null);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(patch(REQUEST_URL, httpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.EMPTY_REQUEST.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomHttpRefTest_shouldReturnDeletedHttpRefIdAnd204() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$", is(httpRef.getId().intValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomHttpRefTest_shouldReturnNotFoundAnd400_whenHttpRefNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        long wrongHttpRefId = httpRef.getId() + 1;

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, wrongHttpRefId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenDefaultHttpRefDeleteRequested() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, false);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void deleteCustomHttpRefTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatch() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        User userMismatch = dataHelper.createUser("two", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, userMismatch);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(delete(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomHttpRefByIdTest_shouldReturnHttpRefResponseDtoAnd200() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(get(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(httpRef.getId().intValue())))
                .andExpect(jsonPath("$.name", is(httpRef.getName())))
                .andExpect(jsonPath("$.description", is(httpRef.getDescription())))
                .andExpect(jsonPath("$.ref", is(httpRef.getRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomHttpRefByIdTest_shouldReturnNotFoundAnd400_whenHttpRefNotFound() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, user);

        long wrongHttpRefId = httpRef.getId() + 1;

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(get(REQUEST_URL, wrongHttpRefId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NOT_FOUND.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomHttpRefByIdTest_shouldReturnErrorMessageAnd400_whenDefaultHttpRefRequested() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, false);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(get(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.DEFAULT_MEDIA_REQUESTED.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "username-one", password = "password-one", roles = "USER")
    void getCustomHttpRefByIdTest_shouldReturnErrorMessageAnd400_whenUserResourceMismatch() throws Exception {
        // Given
        Role role = dataHelper.createRole("ROLE_USER");
        Country country = dataHelper.createCountry(1);
        User user = dataHelper.createUser("one", role, country, null, 20);
        User userMismatch = dataHelper.createUser("two", role, country, null, 20);

        HttpRef httpRef = dataHelper.createHttpRef(1, true);
        dataHelper.httpRefAddUser(httpRef, userMismatch);

        String REQUEST_URL = URL + "/{httpRefId}";

        // When
        mockMvc.perform(get(REQUEST_URL, httpRef.getId()).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.USER_RESOURCE_MISMATCH.getName())))
                .andExpect(jsonPath("$.code", is(HttpStatus.BAD_REQUEST.value())))
                .andDo(print());
    }
}
