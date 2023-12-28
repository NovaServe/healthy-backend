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
import healthy.lifestyle.backend.config.BeanConfig;
import healthy.lifestyle.backend.config.ContainerConfig;
import healthy.lifestyle.backend.exception.ApiException;
import healthy.lifestyle.backend.exception.ErrorMessage;
import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.util.DbUtil;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.URL;
import healthy.lifestyle.backend.workout.dto.HttpRefCreateRequestDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefUpdateRequestDto;
import healthy.lifestyle.backend.workout.model.HttpRef;
import java.util.List;
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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

    @Test
    void getDefaultHttpRefsTest_shouldReturnDefaultHttpRefDtoListWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.DEFAULT_HTTP_REFS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());
        assertThat(responseDto)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(List.of(defaultHttpRef1, defaultHttpRef2));
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefsTest_shouldReturnCustomHttpRefDtoListWith200_whenValidRequest() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);

        User user1 = dbUtil.createUser(1, role, country);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(2, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(3, user1);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(3, user2);
        HttpRef customHttpRef4 = dbUtil.createCustomHttpRef(4, user2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        List<HttpRefResponseDto> responseDto =
                objectMapper.readValue(responseContent, new TypeReference<List<HttpRefResponseDto>>() {});

        assertEquals(2, responseDto.size());

        assertThat(responseDto)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("exercises", "user")
                .isEqualTo(List.of(customHttpRef1, customHttpRef2));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomHttpRefsTest_shouldReturnEmptyListWith200_whenNoHttpRefsFound() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);

        User user1 = dbUtil.createUser(1, role, country);

        User user2 = dbUtil.createUser(2, role, country);
        HttpRef customHttpRef3 = dbUtil.createCustomHttpRef(1, user2);
        HttpRef customHttpRef4 = dbUtil.createCustomHttpRef(2, user2);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_HTTP_REFS).contentType(MediaType.APPLICATION_JSON))

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
    void getCustomHttpRefsTest_shouldReturnVoidWith401_whenUserNotAuthorized() throws Exception {
        // When
        mockMvc.perform(get(URL.CUSTOM_HTTP_REFS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // todo: add parametrization for valid input
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomHttpRefTest_shouldReturnErrorMessageWith400_whenEmptyRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        HttpRef customHttpRef = dbUtil.createCustomHttpRef(1, user);

        HttpRefUpdateRequestDto requestDto = dtoUtil.httpRefUpdateRequestDtoEmpty();
        ApiException expectedException = new ApiException(ErrorMessage.EMPTY_REQUEST, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_HTTP_REF_ID, customHttpRef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
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
                .andExpect(jsonPath("$.code", is(expectedException.getHttpStatusValue())))
                .andDo(print());
    }
}
