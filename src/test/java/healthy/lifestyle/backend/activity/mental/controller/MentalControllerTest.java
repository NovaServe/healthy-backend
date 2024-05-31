package healthy.lifestyle.backend.activity.mental.controller;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import healthy.lifestyle.backend.activity.mental.dto.MentalCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.model.Mental;
import healthy.lifestyle.backend.activity.mental.model.MentalType;
import healthy.lifestyle.backend.activity.workout.model.HttpRef;
import healthy.lifestyle.backend.shared.exception.ApiException;
import healthy.lifestyle.backend.shared.exception.ErrorMessage;
import healthy.lifestyle.backend.testconfig.BeanConfig;
import healthy.lifestyle.backend.testconfig.ContainerConfig;
import healthy.lifestyle.backend.testutil.DbUtil;
import healthy.lifestyle.backend.testutil.DtoUtil;
import healthy.lifestyle.backend.testutil.URL;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
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
public class MentalControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void getDefaultMentalById_shouldReturnDefaultMentalDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        Mental customMental1 =
                dbUtil.createCustomMental(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);
        Mental customMental2 =
                dbUtil.createCustomMental(4, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.DEFAULT_MENTAL_ID, defaultMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "mentalTypeId")
                .isEqualTo(defaultMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("mentals", "httpRefTypeName")
                .isEqualTo(defaultMental1.getHttpRefsSortedById());
    }

    @Test
    void getDefaultMentalById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentDefaultMentalId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentDefaultMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_MENTAL_ID, nonExistentDefaultMentalId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        Mental customMental1 =
                dbUtil.createCustomMental(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);
        Mental customMental2 =
                dbUtil.createCustomMental(4, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.CUSTOM_MENTAL_ID, customMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(customMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "mentals", "httpRefTypeName")
                .isEqualTo(customMental1.getHttpRefsSortedById());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentCustomMentalId = 1000L;
        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentCustomMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_ID, nonExistentCustomMentalId).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalById_shouldReturnErrorMessageWith400_whenMentalUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental = dbUtil.createCustomMental(3, List.of(defaultHttpRef1), mentalType1, user2);
        ApiException expectedException =
                new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, customMental.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_ID, customMental.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    void getAllMentals_shouldReturnListDefaultMentalsWith200_whenValidRequest() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        String sortDirection = "ASC";
        String sortField = "title";

        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType1);
        Mental customMental = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user);

        List<Mental> expectedMentalList = Stream.of(defaultMental1, defaultMental2, defaultMental3, defaultMental4)
                .sorted(Comparator.comparingLong(Mental::getId))
                .toList();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.ALL_MENTALS)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(expectedMentalList.size())))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getAllMentals_shouldReturnListMentalsDtoWith200_whenValidRequest() throws Exception {
        // Given
        int pageNumber = 0;
        int pageSize = 10;
        String sortDirection = "ASC";
        String sortField = "title";

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user1);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user2);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType2);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef1), mentalType2);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef2), mentalType1);

        Mental customMental1 = dbUtil.createCustomMental(5, List.of(customHttpRef1), mentalType1, user1);
        Mental customMental2 =
                dbUtil.createCustomMental(6, List.of(defaultHttpRef1, customHttpRef1), mentalType2, user1);
        Mental customMental3 =
                dbUtil.createCustomMental(7, List.of(defaultHttpRef2, customHttpRef1), mentalType2, user1);
        Mental customMental4 = dbUtil.createCustomMental(8, List.of(customHttpRef2), mentalType1, user2);
        Mental customMental5 =
                dbUtil.createCustomMental(9, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user2);

        List<Mental> expectedMentalList = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        defaultMental3,
                        defaultMental4,
                        customMental1,
                        customMental2,
                        customMental3)
                .sorted(Comparator.comparingLong(Mental::getId))
                .toList();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.ALL_MENTALS)
                        .param("sortField", sortField)
                        .param("sortDirection", sortDirection)
                        .param("pageSize", String.valueOf(pageSize))
                        .param("pageNumber", String.valueOf(pageNumber))
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(expectedMentalList.size())))
                .andExpect(jsonPath("$.size", is(pageSize)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }

    @ParameterizedTest
    @MethodSource("updateCustomMentalValidFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnUpdatedDtoWith200_whenValidFilters(
            String updateTitle, String updateDescription, String updatedMentalType) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        HttpRef customHttpRefToAdd = dbUtil.createCustomHttpRef(5, user);
        HttpRef defaultHttpRefToAdd = dbUtil.createDefaultHttpRef(6);

        Optional<Long> mentalType = Stream.of(mentalType1, mentalType2)
                .filter(mentalTypes -> mentalTypes.getName().equals(updatedMentalType))
                .map(MentalType::getId)
                .findFirst();
        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDto(
                2,
                List.of(
                        customHttpRef1.getId(),
                        defaultHttpRef1.getId(),
                        customHttpRefToAdd.getId(),
                        defaultHttpRefToAdd.getId()),
                mentalType1.getId());
        requestDto.setTitle(updateTitle);
        requestDto.setDescription(updateDescription);
        mentalType.ifPresent(requestDto::setMentalTypeId);

        String initialTitle = customMental.getTitle();
        String initialDescription = customMental.getDescription();
        Long initialNeedsMental = customMental.getType().getId();

        // Expected nested objects
        List<HttpRef> expectedHttpRefs =
                List.of(customHttpRef1, defaultHttpRef1, customHttpRefToAdd, defaultHttpRefToAdd);

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto = objectMapper.readValue(responseContent, MentalResponseDto.class);

        assertTrue(responseDto.isCustom());

        if (nonNull(updateTitle)) assertEquals(requestDto.getTitle(), responseDto.getTitle());
        else assertEquals(initialTitle, responseDto.getTitle());

        if (nonNull(updateDescription)) assertEquals(requestDto.getDescription(), responseDto.getDescription());
        else assertEquals(initialDescription, responseDto.getDescription());

        if (mentalType.isPresent()) assertEquals(requestDto.getMentalTypeId(), responseDto.getMentalTypeId());
        else assertEquals(initialNeedsMental, responseDto.getMentalTypeId());
        assertThat(responseDto.getHttpRefs())
                .usingRecursiveComparison()
                .ignoringFields("httpRefTypeName")
                .isEqualTo(expectedHttpRefs);
    }

    static Stream<Arguments> updateCustomMentalValidFilters() {
        return Stream.of(
                Arguments.of("Update title", "Update description", "AFFIRMATION"),
                Arguments.of("Update title", "Update description", null),
                Arguments.of("Update title", null, "AFFIRMATION"),
                Arguments.of(null, "Update description", "AFFIRMATION"),
                Arguments.of("Update title", "Update description", "MEDITATION"),
                Arguments.of("Update title", "Update description", null),
                Arguments.of("Update title", null, "MEDITATION"),
                Arguments.of(null, "Update description", "MEDITATION"));
    }

    @ParameterizedTest
    @MethodSource("updateCustomMentalInvalidFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnValidationMessageWith400_whenInvalidFilters(
            String updateTitle,
            String updateDescription,
            List<Long> updateHttpRefsIds,
            String updatedMentalType,
            String errorFieldName,
            String errorMessage)
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();

        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        Optional<Long> mentalType = Stream.of(mentalType1, mentalType2)
                .filter(mentalTypes -> mentalTypes.getName().equals(updatedMentalType))
                .map(MentalType::getId)
                .findFirst();

        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDtoEmpty();
        requestDto.setTitle(updateTitle);
        requestDto.setDescription(updateDescription);
        requestDto.setHttpRefIds(updateHttpRefsIds);
        mentalType.ifPresent(requestDto::setMentalTypeId);

        // When
        mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$." + errorFieldName, is(errorMessage)))
                .andDo(print());
    }

    static Stream<Arguments> updateCustomMentalInvalidFilters() {
        return Stream.of(
                // Invalid title
                Arguments.of(
                        "Update title!",
                        null,
                        Collections.emptyList(),
                        "MEDITATION",
                        "title",
                        "Title can include lower and upper-case letters, digits, spaces, and symbols . , - ( ) /"),
                // Invalid description
                Arguments.of(
                        null,
                        "Update description^",
                        Collections.emptyList(),
                        "MEDITATION",
                        "description",
                        "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + ="));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnUpdatedDtoWith200_whenHttpRefsIdsNotGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        // Http refs should be removed from the target exercise. Other fields should remain the same.
        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDtoEmpty();
        List<Long> newHttpRefs = Collections.emptyList();
        requestDto.setHttpRefIds(newHttpRefs);
        requestDto.setMentalTypeId(customMental.getType().getId());

        // When
        MvcResult mvcResult = mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto = objectMapper.readValue(responseContent, MentalResponseDto.class);

        assertTrue(responseDto.isCustom());
        assertEquals(customMental.getTitle(), responseDto.getTitle());
        assertEquals(customMental.getDescription(), responseDto.getDescription());
        assertEquals(customMental.getType().getId(), responseDto.getMentalTypeId());
        assertThat(responseDto.getHttpRefs()).usingRecursiveComparison().isEqualTo(Collections.emptyList());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnErrorMessageWith404_whenCustomMentalNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        long nonExistentMentalId = 1000L;

        MentalUpdateRequestDto requestDto =
                dtoUtil.mentalUpdateRequestDto(2, List.of(defaultHttpRef.getId()), mentalType2.getId());

        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, nonExistentMentalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnErrorMessageWith400_whenEmptyRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDtoEmpty();
        requestDto.setMentalTypeId(customMental.getType().getId());
        requestDto.setHttpRefIds(customMental.getHttpRefsIdsSorted());

        // When
        mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(ErrorMessage.NO_UPDATES_REQUEST.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnErrorMessageWith400_whenMentalUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user = dbUtil.createUser(1, role, country);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental1 = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        User user2 = dbUtil.createUser(2, role, country);
        Mental customMental2 =
                dbUtil.createCustomMental(2, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user2);

        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDtoEmpty();
        requestDto.setMentalTypeId(customMental1.getType().getId());
        requestDto.setHttpRefIds(Collections.emptyList());

        ApiException expectedException =
                new ApiException(ErrorMessage.USER_MENTAL_MISMATCH, customMental2.getId(), HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMental_shouldReturnErrorMessageWith400_whenMentalWithNewTitleAlreadyExists() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        Mental customMental1 = dbUtil.createCustomMental(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        Mental customMental2 =
                dbUtil.createCustomMental(2, List.of(customHttpRef1, defaultHttpRef1), mentalType2, user);

        MentalUpdateRequestDto requestDto = dtoUtil.mentalUpdateRequestDtoEmpty();

        requestDto.setMentalTypeId(customMental1.getType().getId());
        requestDto.setHttpRefIds(Collections.emptyList());
        requestDto.setTitle(customMental2.getTitle());

        ApiException expectedException = new ApiException(ErrorMessage.TITLE_DUPLICATE, null, HttpStatus.BAD_REQUEST);

        // When
        mockMvc.perform(patch(URL.CUSTOM_MENTAL_ID, customMental1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(expectedException.getMessage())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomMental_shouldReturnVoidWith204_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        Mental customMental1 =
                dbUtil.createCustomMental(1, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user);

        long mentalIdToBeRemoved = customMental1.getId();

        // When
        mockMvc.perform(delete(URL.CUSTOM_MENTAL_ID, customMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        assertNull(dbUtil.getMentalById(mentalIdToBeRemoved));
        assertTrue(dbUtil.httpRefsExistByIds(List.of(customHttpRef1.getId(), defaultHttpRef1.getId())));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomMental_shouldReturnErrorMessageWith404_whenMentalNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        Mental customMental1 =
                dbUtil.createCustomMental(1, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user);
        long nonExistentMentalId = 1000L;

        ApiException expectedException =
                new ApiException(ErrorMessage.MENTAL_NOT_FOUND, nonExistentMentalId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(delete(URL.CUSTOM_MENTAL_ID, nonExistentMentalId).contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMental_ReturnVoidWith204_whenValidRequest() throws Exception {
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        MentalCreateRequestDto customMentalRequestDto = dtoUtil.mentalCreateRequestDto(
                1, List.of(customHttpRef1.getId(), defaultHttpRef1.getId()), mentalType1.getId());

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTALS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customMentalRequestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(customMentalRequestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(customMentalRequestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto = objectMapper.readValue(responseContent, MentalResponseDto.class);

        assertEquals(
                customMentalRequestDto.getHttpRefs().size(),
                responseDto.getHttpRefs().size());
        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "exercises", "user", "mentals", "nutritions", "httpRefTypeName")
                .isEqualTo(List.of(customHttpRef1, defaultHttpRef1));

        // Db
        Mental createdMental = dbUtil.getMentalById(responseDto.getId());
        assertEquals(responseDto.getId(), createdMental.getId());
        assertEquals(customMentalRequestDto.getTitle(), createdMental.getTitle());
        assertEquals(customMentalRequestDto.getDescription(), createdMental.getDescription());
        assertEquals(
                customMentalRequestDto.getMentalTypeId(),
                createdMental.getType().getId());
        assertTrue(createdMental.isCustom());
        assertEquals(user.getId(), createdMental.getUser().getId());

        assertEquals(
                customMentalRequestDto.getHttpRefs().size(),
                createdMental.getHttpRefs().size());

        assertThat(createdMental.getHttpRefsSortedById())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "exercises", "user", "mentals", "nutritions", "httpRefType")
                .isEqualTo(List.of(customHttpRef1, defaultHttpRef1));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMental_ReturnVoidWith201_whenValidMandatoryFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalCreateRequestDto customMentalRequestDto =
                dtoUtil.mentalCreateRequestDto(1, Collections.emptyList(), mentalType1.getId());
        customMentalRequestDto.setDescription(null);

        // When
        MvcResult mvcResult = mockMvc.perform(post(URL.CUSTOM_MENTALS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customMentalRequestDto)))

                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.title", is(customMentalRequestDto.getTitle())))
                .andExpect(jsonPath("$.description", is(customMentalRequestDto.getDescription())))
                .andExpect(jsonPath("$.isCustom", is(true)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalResponseDto responseDto = objectMapper.readValue(responseContent, MentalResponseDto.class);

        assertTrue(customMentalRequestDto.getHttpRefs().isEmpty());
        assertEquals(customMentalRequestDto.getMentalTypeId(), responseDto.getMentalTypeId());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMental_shouldReturnErrorMessageWith400_whenHttpRefNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        long nonExistentHttpRefId = 1000L;
        MentalCreateRequestDto mentalCreateRequestDto =
                dtoUtil.mentalCreateRequestDto(1, Collections.singletonList(nonExistentHttpRefId), mentalType1.getId());
        ApiException expectedException =
                new ApiException(ErrorMessage.HTTP_REF_NOT_FOUND, nonExistentHttpRefId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(post(URL.CUSTOM_MENTALS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mentalCreateRequestDto)))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("getMentalsWithFilterValidDefaultFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilter_shouldReturnDefaultFilteredPageWith200_whenValidFilters(
            String title,
            String description,
            String mentalType,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {

        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        HttpRef defaultHttpRef3 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef4 = dbUtil.createDefaultHttpRef(4);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef2), mentalType1);
        Mental defaultMental3 = dbUtil.createDefaultMental(3, List.of(defaultHttpRef3), mentalType2);
        Mental defaultMental4 = dbUtil.createDefaultMental(4, List.of(defaultHttpRef4, defaultHttpRef3), mentalType2);

        User user = dbUtil.createUser(1);

        Mental customMental1 =
                dbUtil.createCustomMental(5, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        Mental customMental2 =
                dbUtil.createCustomMental(6, List.of(defaultHttpRef3, defaultHttpRef2), mentalType1, user);

        List<Mental> expectedFilteredMentals = Stream.of(
                        defaultMental1, defaultMental2, defaultMental3, defaultMental4, customMental1, customMental2)
                .filter(mental ->
                        resultSeeds.stream().anyMatch(seed -> mental.getTitle().contains(String.valueOf(seed))))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";

        Optional<String> metalFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(mentalType))
                .map(type -> type.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTALS)
                        .param("isCustom", String.valueOf(false))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", metalFilter.orElse(null))
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
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        Assertions.assertEquals(mentalResponseDtoList.size(), resultSeeds.size());
    }

    static Stream<Arguments> getMentalsWithFilterValidDefaultFilters() {
        return Stream.of(
                // Default, positive
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 2L)),
                Arguments.of("mental", null, "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 2L)),
                Arguments.of(null, "Desc ", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 2L)),
                Arguments.of("mental", "Desc ", "AFFIRMATION", 2, 0, 2, 1, 2, List.of(1L, 2L)),
                Arguments.of(null, null, "MEDITATION", 2, 0, 2, 1, 2, List.of(3L, 4L)),
                Arguments.of("mental", null, "MEDITATION", 2, 0, 2, 1, 2, List.of(3L, 4L)),
                Arguments.of(null, "Desc ", "MEDITATION", 2, 0, 2, 1, 2, List.of(3L, 4L)),
                Arguments.of("mental", "Desc ", "MEDITATION", 2, 0, 2, 1, 2, List.of(3L, 4L)),

                // Default, empty
                Arguments.of("non existent", null, "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non existent", "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @ParameterizedTest
    @MethodSource("getMentalsWithFilterValidCustomFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilter_shouldReturnCustomFilteredPageWith200_whenValidFilters(
            String title,
            String description,
            String mentalType,
            int pageSize,
            int pageNumber,
            int totalElements,
            int totalPages,
            int numberOfElementsCurrentPage,
            List<Long> resultSeeds)
            throws Exception {

        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef1), mentalType2, user1);
        Mental customMental1User2 = dbUtil.createCustomMental(7, List.of(defaultHttpRef1), mentalType1, user2);
        Mental customMental2User2 = dbUtil.createCustomMental(8, List.of(defaultHttpRef1), mentalType2, user2);

        List<Mental> expectedFilteredMentals = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        customMental1User1,
                        customMental2User1,
                        customMental1User2,
                        customMental2User2)
                .filter(mental ->
                        resultSeeds.stream().anyMatch(seed -> mental.getTitle().contains(String.valueOf(seed))))
                .toList();

        String sortDirection = "ASC";
        String sortField = "id";

        Optional<String> metalFilter = Stream.of(mentalType1, mentalType2)
                .filter(type -> type.getName().equals(mentalType))
                .map(type -> type.getId().toString())
                .findFirst();

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTALS)
                        .param("isCustom", String.valueOf(true))
                        .param("title", title)
                        .param("description", description)
                        .param("mentalTypeId", metalFilter.orElse(null))
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
                .andExpect(jsonPath("$.numberOfElements", is(numberOfElementsCurrentPage)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        assertEquals(expectedFilteredMentals.size(), mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "httpRefs", "mentalTypeId", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentals);
    }

    static Stream<Arguments> getMentalsWithFilterValidCustomFilters() {
        return Stream.of(
                // Custom, positive
                Arguments.of(null, null, "AFFIRMATION", 2, 0, 1, 1, 1, List.of(6L)),
                Arguments.of("mental", null, "AFFIRMATION", 2, 0, 1, 1, 1, List.of(6L)),
                Arguments.of(null, "Desc ", "AFFIRMATION", 2, 0, 1, 1, 1, List.of(6L)),
                Arguments.of("mental", "Desc ", "AFFIRMATION", 2, 0, 1, 1, 1, List.of(6L)),
                Arguments.of(null, null, "MEDITATION", 2, 0, 1, 1, 1, List.of(5L)),
                Arguments.of("mental", null, "MEDITATION", 2, 0, 1, 1, 1, List.of(5L)),
                Arguments.of(null, "Desc ", "MEDITATION", 2, 0, 1, 1, 1, List.of(5L)),
                Arguments.of("mental", "Desc ", "MEDITATION", 2, 0, 1, 1, 1, List.of(5L)),

                // Custom, empty
                Arguments.of("non existent", null, "AFFIRMATION", 2, 0, 0, 0, 0, Collections.emptyList()),
                Arguments.of(null, "non existent", "MEDITATION", 2, 0, 0, 0, 0, Collections.emptyList()));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilter_shouldReturnDefaultAndCustomFilteredPageWith200() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef1), mentalType2, user1);
        Mental customMental1User2 = dbUtil.createCustomMental(7, List.of(defaultHttpRef1), mentalType1, user2);
        Mental customMental2User2 = dbUtil.createCustomMental(8, List.of(defaultHttpRef1), mentalType2, user2);

        List<Mental> expectedFilteredMentals =
                List.of(defaultMental1, defaultMental2, customMental1User1, customMental2User1);

        // When
        MvcResult mvcResult = mockMvc.perform(get(URL.CUSTOM_MENTALS).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.numberOfElements", is(4)))
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode contentNode = rootNode.path("content");
        List<MentalResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalResponseDto>>() {});
        assertEquals(4, mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "httpRefs", "mentalTypeId", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentals);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalsWithFilter_shouldReturnValidationErrorMessageWith400_whenInvalidFilters() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        Mental defaultMental1 = dbUtil.createDefaultMental(1, List.of(defaultHttpRef1), mentalType1);
        Mental defaultMental2 = dbUtil.createDefaultMental(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        Mental customMental1User1 = dbUtil.createCustomMental(5, List.of(defaultHttpRef1), mentalType1, user1);
        Mental customMental2User1 = dbUtil.createCustomMental(6, List.of(defaultHttpRef1), mentalType2, user1);
        Mental customMental1User2 = dbUtil.createCustomMental(7, List.of(defaultHttpRef1), mentalType1, user2);
        Mental customMental2User2 = dbUtil.createCustomMental(8, List.of(defaultHttpRef1), mentalType2, user2);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTALS)
                        .param("title", "title!")
                        .param("description", "description^")
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.title",
                        is("Title can include lower and upper-case letters, digits, spaces, and symbols . , - ( ) /")))
                .andExpect(
                        jsonPath(
                                "$.description",
                                is(
                                        "Description can include lower and upper-case letters, digits, spaces, and symbols: . , - : ; ! ? ' \" # % ( ) + =")))
                .andDo(print());
    }
}
