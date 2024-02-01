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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
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
    void createCustomHttpRefTest_shouldReturnHttpRefDtoWith201_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

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
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRefTest_shouldReturnErrorMessageWith400_whenAlreadyExistsWithSameName() throws Exception {
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
    void createCustomHttpRefTest_shouldReturnValidationMessageWith400_whenTooShortNameGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        String invalidValue = "abc";
        createHttpRequestDto.setName(invalidValue);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Size should be from 5 to 255 characters long")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageWith400_whenInvalidNameGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        String invalidValue = "abc@def#";
        createHttpRequestDto.setName(invalidValue);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageWith400_whenInvalidDescriptionGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        String invalidValue = "abc@def#";
        createHttpRequestDto.setDescription(invalidValue);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", is("Not allowed symbols")))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomHttpRefTest_shouldReturnValidationMessageWith400_whenInvalidRefGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        HttpRefCreateRequestDto createHttpRequestDto = dtoUtil.httpRefCreateRequestDto(1);
        String invalidValue = "abc@def#";
        createHttpRequestDto.setRef(invalidValue);

        // When
        mockMvc.perform(post(URL.CUSTOM_HTTP_REFS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createHttpRequestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ref", is("Invalid format, should start with http")))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("multipleValidFiltersDefaultHttpRefs")
    void getDefaultHttpRefsWithFilterTest_shouldReturnDefaultHttpRefsWith200_whenValidRequest(
            String name, String description, int totalElements, int totalPages, int numberOfElements) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
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

    static Stream<Arguments> multipleValidFiltersDefaultHttpRefs() {
        return Stream.of(
                // Default
                Arguments.of("1", null, 1, 1, 1),
                Arguments.of(null, "1", 1, 1, 1),
                Arguments.of("1", "1", 1, 1, 1),
                Arguments.of("1", "2", 0, 0, 0),
                Arguments.of("Name", null, 3, 2, 2),
                Arguments.of(null, "Desc", 3, 2, 2),
                Arguments.of(null, null, 3, 2, 2));
    }

    @ParameterizedTest
    @MethodSource("multipleFiltersDefaultSortAndOrder")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getDefaultHttpRefsWithFilterTest_shouldReturnDefaultHttpRefsWith200_whenSortAndOrder(
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
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef3);
        }

        if (sortDirection.equals("DESC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);
        }
    }

    static Stream<Arguments> multipleFiltersDefaultSortAndOrder() {
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
    @MethodSource("multipleInvalidFiltersDefaultHttpRefs")
    void getDefaultHttpRefsWithFilterTest_shouldReturnValidationErrorMessageWith400_whenInvalidRequest(
            String name, String description, String validationErrorMessage) throws Exception {
        // When
        mockMvc.perform(get(URL.DEFAULT_HTTP_REFS)
                        .param("name", name)
                        .param("description", description)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(validationErrorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> multipleInvalidFiltersDefaultHttpRefs() {
        return Stream.of(
                // Default
                Arguments.of("!invalid-name", null, "name: Not allowed symbols"),
                Arguments.of(null, "!invalid-description", "description: Not allowed symbols"));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefByIdTest_shouldReturnHttpRefDtoWith200_whenValidRequest() throws Exception {
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
    void getCustomHttpRefByIdTest_shouldReturnErrorMessageWith404_whenHttpRefNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
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
    void getCustomHttpRefByIdTest_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequestedInsteadOfCustom()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
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
    void getCustomHttpRefByIdTest_shouldReturnErrorMessageWith400_whenHttpRefDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
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
    @MethodSource("multipleValidFiltersDefaultOrCustom")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnDefaultOrCustomHttpRefs(
            Boolean isCustom, String name, String description, int totalElements, int totalPages, int numberOfElements)
            throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country);
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

    static Stream<Arguments> multipleValidFiltersDefaultOrCustom() {
        return Stream.of(
                // Default
                Arguments.of(false, "1", null, 1, 1, 1),
                Arguments.of(false, null, "1", 1, 1, 1),
                Arguments.of(false, "1", "1", 1, 1, 1),
                Arguments.of(false, "1", "2", 0, 0, 0),
                Arguments.of(false, "Name", null, 3, 2, 2),
                Arguments.of(false, null, "Desc", 3, 2, 2),
                Arguments.of(false, null, null, 3, 2, 2),

                // Custom
                Arguments.of(true, "4", null, 1, 1, 1),
                Arguments.of(true, null, "4", 1, 1, 1),
                Arguments.of(true, "4", "4", 1, 1, 1),
                Arguments.of(true, "1", "2", 0, 0, 0),
                Arguments.of(true, "Name", null, 2, 1, 2),
                Arguments.of(true, null, "Desc", 2, 1, 2),
                Arguments.of(true, null, null, 2, 1, 2));
    }

    @ParameterizedTest
    @MethodSource("multipleFiltersDefaultOrCustomSortAndOrder")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnDefaultOrCustomHttpRefs_whenSortAndOrder(
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
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef3);
        }

        if (sortDirection.equals("DESC") && !isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);
        }

        if (sortDirection.equals("ASC") && isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef3);
        }

        if (sortDirection.equals("DESC") && isCustom) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef3);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef1);
        }
    }

    static Stream<Arguments> multipleFiltersDefaultOrCustomSortAndOrder() {
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
    @MethodSource("multipleInvalidFiltersDefaultOrCustomHttpRefs")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnValidationErrorMessageWith400_whenInvalidRequest(
            String name, String description, String validationErrorMessage) throws Exception {
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
                .andExpect(jsonPath("$.message", is(validationErrorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> multipleInvalidFiltersDefaultOrCustomHttpRefs() {
        return Stream.of(
                Arguments.of("!invalid-name", null, "name: Not allowed symbols"),
                Arguments.of(null, "!invalid-description", "description: Not allowed symbols"));
    }

    @ParameterizedTest
    @MethodSource("multipleValidFiltersDefaultAndCustom")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnDefaultAndCustomHttpRefs(
            String name, String description, int totalElements, int totalPages, int numberOfElements) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(4, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(5, user1);
        User user2 = dbUtil.createUser(2, role, country);
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

    static Stream<Arguments> multipleValidFiltersDefaultAndCustom() {
        return Stream.of(
                Arguments.of("1", "2", 0, 0, 0),
                Arguments.of("Name", null, 5, 3, 2),
                Arguments.of(null, "Desc", 5, 3, 2),
                Arguments.of(null, null, 5, 3, 2));
    }

    @ParameterizedTest
    @MethodSource("multipleFiltersDefaultAndCustomSortAndOrder")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getHttpRefsWithFilterTest_shouldReturnDefaultAndCustomHttpRefs_whenSortAndOrder(
            String sortField, String sortDirection) throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user1);
        User user2 = dbUtil.createUser(2, role, country);
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
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef2);
        }

        if (sortDirection.equals("DESC")) {
            assertThat(httpRefResponseDtoList.get(0))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef2);

            assertThat(httpRefResponseDtoList.get(1))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(customHttpRef1);

            assertThat(httpRefResponseDtoList.get(2))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef2);

            assertThat(httpRefResponseDtoList.get(3))
                    .usingRecursiveComparison()
                    .ignoringFields("exercises", "user")
                    .isEqualTo(defaultHttpRef1);
        }
    }

    static Stream<Arguments> multipleFiltersDefaultAndCustomSortAndOrder() {
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
    void updateCustomHttpRefTest_shouldReturnHttpRefDtoWith200_whenValidRequest() throws Exception {
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
    void updateCustomHttpRefTest_shouldReturnErrorMessageWith404_whenHttpRefNotFound() throws Exception {
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
    void updateCustomHttpRefTest_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequestedInsteadOfCustom()
            throws Exception {
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
    void updateCustomHttpRefTest_shouldReturnErrorMessageWith400_whenHttpRefDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user1);

        User user2 = dbUtil.createUser(2, role, country);
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
    void updateCustomHttpRefTest_shouldReturnErrorMessageWith400_whenRequestWithNoUpdates() throws Exception {
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
    void deleteCustomHttpRefTest_shouldReturnVoidWith204_whenValidRequest() throws Exception {
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
    void deleteCustomHttpRefTest_shouldReturnErrorMessageWith404_whenHttpRefNotFound() throws Exception {
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
    void deleteCustomHttpRefTest_shouldReturnErrorMessageWith400_whenDefaultHttpRefRequestedInsteadOfCustom()
            throws Exception {
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
    void deleteCustomHttpRefTest_shouldReturnErrorMessageWith400_whenHttpRefDoesntBelongToUser() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user1);

        User user2 = dbUtil.createUser(2, role, country);
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
