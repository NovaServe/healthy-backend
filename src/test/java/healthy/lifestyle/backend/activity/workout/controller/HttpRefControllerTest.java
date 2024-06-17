package healthy.lifestyle.backend.activity.workout.controller;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.activity.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.shared.validation.ValidationMessage;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.Timezone;
import healthy.lifestyle.backend.user.model.User;
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
class HttpRefControllerTest {
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
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRef_shouldReturnCreatedDtoWith201_whenValidFields() throws Exception {
        // Given
        dbUtil.createUser(1);
        dbUtil.createDefaultHttpRef(1);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(createHttpRequestDto.getName())))
                .andExpect(jsonPath("$.description", is(createHttpRequestDto.getDescription())))
                .andExpect(jsonPath("$.ref", is(createHttpRequestDto.getRef())))
                .andExpect(jsonPath("$.httpRefTypeName", is(createHttpRequestDto.getHttpRefType())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("createCustomHttpRefInvalidFields")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRef_shouldReturnValidationMessageWith400_whenInvalidFields(
            String name, String description, String ref, String fieldName, String validationMessage) throws Exception {
        // Given
        dbUtil.createUser(1);
        dbUtil.createDefaultHttpRef(1);
        HttpRefCreateRequestDto requestDto = dtoUtil.httpRefCreateRequestDtoEmpty();
        if (name != null) requestDto.setName(name);
        if (description != null) requestDto.setDescription(description);
        if (ref != null) requestDto.setRef(ref);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + fieldName, is(validationMessage)))
                .andDo(print());
    }

    static Stream<Arguments> createCustomHttpRefInvalidFields() {
        String lengthMessage = String.format(ValidationMessage.LENGTH_RANGE.getName(), 5, 255);
        String descriptionMessage =
                "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + =";
        String refMessage = "Web link should start with http:// or https://";

        return Stream.of(
                // name
                Arguments.of(null, null, "https://test-ref.com", "name", lengthMessage),
                Arguments.of("", null, "https://test-ref.com", "name", lengthMessage),
                Arguments.of(" ", null, "https://test-ref.com", "name", lengthMessage),
                Arguments.of("name", null, "https://test-ref.com", "name", lengthMessage),
                // description
                Arguments.of("test name", "", "https://test-ref.com", "description", lengthMessage),
                Arguments.of("test name", " ", "https://test-ref.com", "description", lengthMessage),
                Arguments.of("test name", "description^", "https://test-ref.com", "description", descriptionMessage),
                // ref
                Arguments.of("test name", "description", "test-ref.com", "ref", refMessage),
                Arguments.of("test name", "description", null, "ref", ValidationMessage.NOT_NULL_OR_BLANK.getName()),
                Arguments.of("test name", "description", "", "ref", ValidationMessage.NOT_NULL_OR_BLANK.getName()),
                Arguments.of("test name", "description", " ", "ref", ValidationMessage.NOT_NULL_OR_BLANK.getName()));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRef_shouldReturnErrorMessageWith400_whenAlreadyExistsWithSameName() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef alreadyExistentHttpRef = dbUtil.createCustomHttpRef(1, user);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        createHttpRequestDto.setName(alreadyExistentHttpRef.getName());

        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefById_shouldReturnDtoWith200_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(3);

        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REF_ID, customHttpRef1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customHttpRef1.getId().intValue())))
                .andExpect(jsonPath("$.name", is(customHttpRef1.getName())))
                .andExpect(jsonPath("$.description", is(customHttpRef1.getDescription())))
                .andExpect(jsonPath("$.ref", is(customHttpRef1.getRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        dbUtil.createUser(1);
        long nonExistentHttpRefId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REF_ID, nonExistentHttpRefId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefById_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequested() throws Exception {
        // Given
        dbUtil.createUser(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        ApiException expectedException = new ApiException(
                ErrorMessage.DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REF_ID, defaultHttpRef.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefById_shouldReturnErrorMessageWith400_whenHttpRefUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);

        User user1 = dbUtil.createUser(1, role, country, timezone);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(1, user2);

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, customHttpRef.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REF_ID, customHttpRef.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("getDefaultHttpRefsValidFilters")
    void getDefaultHttpRefsWithFilter_shouldReturnPageWith200_whenValidFilters(
            String name, String description, int totalElements, int totalPages, int numberOfElements) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);

        int pageNumber = 0;
        int pageSize = 2;
        String sortDirection = "ASC";
        String sortField = "id";

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(numberOfElements, httpRefResponseDtoList.size());
    }

    static Stream<Arguments> getDefaultHttpRefsValidFilters() {
        return Stream.of(
                // Default
                Arguments.of("Media Name 1", null, 1, 1, 1),
                Arguments.of(null, "Description 1", 1, 1, 1),
                Arguments.of("Name 1", "Description 1", 1, 1, 1),
                Arguments.of("Name 1", "Description 2", 0, 0, 0),
                Arguments.of("Media Name", null, 3, 2, 2),
                Arguments.of(null, "Description", 3, 2, 2),
                Arguments.of(null, null, 3, 2, 2));
    }

    @ParameterizedTest
    @MethodSource("getDefaultHttpRefsSortedByFieldAndDirection")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getDefaultHttpRefsWithFilter_shouldReturnPageWith200_whenSortedByFieldAndDirection(
            String sortField, String sortDirection, Boolean isCustom) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user);

        int pageNumber = 0;
        int pageSize = 3;
        int totalPages = 1;
        String name = null;
        String description = null;
        int totalElements = 3;

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(totalElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(totalElements, httpRefResponseDtoList.size());

        if (sortDirection.equals("ASC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef3);
        }

        if (sortDirection.equals("DESC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);
        }
    }

    static Stream<Arguments> getDefaultHttpRefsSortedByFieldAndDirection() {
        return Stream.of(
                // Default
                Arguments.of("id", "ASC", false),
                Arguments.of("name", "ASC", false),
                Arguments.of("description", "ASC", false),
                Arguments.of("id", "DESC", false),
                Arguments.of("name", "DESC", false),
                Arguments.of("description", "DESC", false));
    }

    @ParameterizedTest
    @MethodSource("getDefaultHttpRefsInvalidFilters")
    void getDefaultHttpRefsWithFilterTest_shouldReturnValidationErrorMessageWith400_whenInvalidFilters(
            String name, String description, String paramName, String validationErrorMessage) throws Exception {
        // When
        mockMvc.perform(get(URL.DEFAULT_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + paramName, is(validationErrorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> getDefaultHttpRefsInvalidFilters() {
        String nameMessage = "Title can include lower and upper-case letters, digits, spaces, and symbols . , - ( ) /";
        String descriptionMessage =
                "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + =";

        return Stream.of(
                // Default
                Arguments.of("invalid-name!", null, "name", nameMessage),
                Arguments.of(null, "invalid-description^", "description", descriptionMessage));
    }

    @ParameterizedTest
    @MethodSource("getHttpRefsValidFiltersDefaultOrCustom")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilter_shouldReturnPageOfDefaultOrCustom_whenValidFilters(
            Boolean isCustom, String name, String description, int totalElements, int totalPages, int numberOfElements)
            throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user2);

        int pageNumber = 0;
        int pageSize = 2;
        String sortDirection = "ASC";
        String sortField = "id";

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS)
                        .param("isCustom", String.valueOf(isCustom))
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(numberOfElements, httpRefResponseDtoList.size());
    }

    static Stream<Arguments> getHttpRefsValidFiltersDefaultOrCustom() {
        return Stream.of(
                // Default
                Arguments.of(false, "Name 1", null, 1, 1, 1),
                Arguments.of(false, null, "Description 1", 1, 1, 1),
                Arguments.of(false, "Name 1", "Description 1", 1, 1, 1),
                Arguments.of(false, "Name 1", "Description 2", 0, 0, 0),
                Arguments.of(false, "Media Name", null, 3, 2, 2),
                Arguments.of(false, null, "Description", 3, 2, 2),
                Arguments.of(false, null, null, 3, 2, 2),

                // Custom
                Arguments.of(true, "Name 4", null, 1, 1, 1),
                Arguments.of(true, null, "Description 4", 1, 1, 1),
                Arguments.of(true, "Name 4", "Description 4", 1, 1, 1),
                Arguments.of(true, "Name 1", "Description 2", 0, 0, 0),
                Arguments.of(true, "Media Name", null, 2, 1, 2),
                Arguments.of(true, null, "Description", 2, 1, 2),
                Arguments.of(true, null, null, 2, 1, 2));
    }

    @ParameterizedTest
    @MethodSource("getHttpRefsWithFilterDefaultOrCustomSortedByFieldAndDirection")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnPageOfDefaultOrCustom_whenSortedByFieldsAndDirection(
            String sortField, String sortDirection, Boolean isCustom) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user);

        int pageNumber = 0;
        int pageSize = 3;
        int totalPages = 1;
        String name = null;
        String description = null;
        int totalElements = 3;

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS)
                        .param("isCustom", String.valueOf(isCustom))
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(totalElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(totalElements, httpRefResponseDtoList.size());

        if (sortDirection.equals("ASC") && !isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef3);
        }

        if (sortDirection.equals("DESC") && !isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);
        }

        if (sortDirection.equals("ASC") && isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef3);
        }

        if (sortDirection.equals("DESC") && isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef1);
        }
    }

    static Stream<Arguments> getHttpRefsWithFilterDefaultOrCustomSortedByFieldAndDirection() {
        return Stream.of(
                // Default
                Arguments.of("id", "ASC", false),
                Arguments.of("name", "ASC", false),
                Arguments.of("description", "ASC", false),
                Arguments.of("id", "DESC", false),
                Arguments.of("name", "DESC", false),
                Arguments.of("description", "DESC", false),

                // Custom
                Arguments.of("id", "ASC", true),
                Arguments.of("name", "ASC", true),
                Arguments.of("description", "ASC", true),
                Arguments.of("id", "DESC", true),
                Arguments.of("name", "DESC", true),
                Arguments.of("description", "DESC", true));
    }

    @ParameterizedTest
    @MethodSource("getHttpRefsWithFilterDefaultOrCustomInvalidFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnValidationErrorMessageWith400_whenInvalidFilters(
            String name, String description, String paramName, String validationErrorMessage) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REFS)
                        .param("isCustom", String.valueOf(true))
                        .param("name", name)
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + paramName, is(validationErrorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> getHttpRefsWithFilterDefaultOrCustomInvalidFilters() {
        String nameMessage = "Title can include lower and upper-case letters, digits, spaces, and symbols . , - ( ) /";
        String descriptionMessage =
                "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + =";

        return Stream.of(
                // Default
                Arguments.of("invalid-name!", null, "name", nameMessage),
                Arguments.of(null, "invalid-description^", "description", descriptionMessage));
    }

    @ParameterizedTest
    @MethodSource("getHttpRefsWithFilterDefaultAndCustomValidFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilter_shouldReturnPageOfDefaultAndCustom_whenValidFilters(
            String name, String description, int totalElements, int totalPages, int numberOfElements) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(6, user2);

        int pageNumber = 0;
        int pageSize = 2;
        String sortDirection = "ASC";
        String sortField = "id";

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(numberOfElements, httpRefResponseDtoList.size());
    }

    static Stream<Arguments> getHttpRefsWithFilterDefaultAndCustomValidFilters() {
        return Stream.of(
                Arguments.of("Name 1", "Description 2", 0, 0, 0),
                Arguments.of("Media Name", null, 5, 3, 2),
                Arguments.of(null, "Description", 5, 3, 2),
                Arguments.of(null, null, 5, 3, 2));
    }

    @ParameterizedTest
    @MethodSource("getHttpRefsWithFilterDefaultAndCustomSortedByFieldAndDirection")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnPageOfDefaultAndCustom_whenSortedByFieldAndDirection(
            String sortField, String sortDirection) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);
        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user1);
        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef4 = dbUtil.createCustomHttpRef(5, user2);

        int pageNumber = 0;
        int pageSize = 4;
        String name = null;
        String description = null;
        int totalElements = 4;
        int totalPages = 1;

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(totalElements)))
                .andExpect(jsonPath("$.totalPages", is(totalPages)))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andExpect(jsonPath("$.numberOfElements", is(totalElements)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<HttpRefResponseDto> httpRefResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<HttpRefResponseDto>>() {});
        assertEquals(totalElements, httpRefResponseDtoList.size());

        if (sortDirection.equals("ASC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef2);
        }

        if (sortDirection.equals("DESC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user", "httpRefTypeName")
                    .isEqualTo(defaultHttpRef1);
        }
    }

    static Stream<Arguments> getHttpRefsWithFilterDefaultAndCustomSortedByFieldAndDirection() {
        return Stream.of(
                Arguments.of("id", "ASC"),
                Arguments.of("name", "ASC"),
                Arguments.of("description", "ASC"),
                Arguments.of("id", "DESC"),
                Arguments.of("name", "DESC"),
                Arguments.of("description", "DESC"));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRef_shouldReturnDtoWith200_whenValidFilters() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(1, user);

        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(2);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, customHttpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(customHttpRef.getId().intValue())))
                .andExpect(jsonPath("$.name", is(requestDto.getName())))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.ref", is(requestDto.getRef())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRef_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long nonExistentHttpRefId = 1000L;
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(1);
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, nonExistentHttpRefId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRef_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequested() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(2);
        ApiException expectedException =
                new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, defaultHttpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRef_shouldReturnErrorMessageWith400_whenHttpRefUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);

        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user1);

        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user2);

        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDto(2);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, customHttpRef2.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, customHttpRef2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRef_shouldReturnErrorMessageWith400_whenRequestWithNoUpdates() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(1, user);

        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDtoEmpty();
        ApiException expectedException =
                new ApiException(ErrorMessage.NO_UPDATES_REQUEST, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, customHttpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomHttpRef_shouldReturnVoidWith204_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(1, user);

        // When
        mockMvc.perform(delete(URL.CUSTOM_HTTP_REF_ID, customHttpRef.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        assertFalse(dbUtil.httpRefsExistByIds(List.of(customHttpRef.getId())));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomHttpRef_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        long nonExistentHttpRefId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(delete(URL.CUSTOM_HTTP_REF_ID, nonExistentHttpRefId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomHttpRef_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequested() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        ApiException expectedException =
                new ApiException(ErrorMessage.DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(delete(URL.CUSTOM_HTTP_REF_ID, defaultHttpRef.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomHttpRef_shouldReturnErrorMessageWith400_whenHttpRefUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        Timezone timezone = dbUtil.createTimezone(1);

        User user1 = dbUtil.createUser(1, role, country, timezone);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user1);

        User user2 = dbUtil.createUser(2, role, country, timezone);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user2);

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_HTTP_REF_MISMATCH, customHttpRef2.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(delete(URL.CUSTOM_HTTP_REF_ID, customHttpRef2.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }
}
