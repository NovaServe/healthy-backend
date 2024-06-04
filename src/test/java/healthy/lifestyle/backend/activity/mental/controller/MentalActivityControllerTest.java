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
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityCreateRequestDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityResponseDto;
import healthy.lifestyle.backend.activity.mental.dto.MentalActivityUpdateRequestDto;
import healthy.lifestyle.backend.activity.mental.model.MentalActivity;
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
public class MentalActivityControllerTest {
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
    void getDefaultMentalActivityById_shouldReturnDefaultMentalActivityDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.DEFAULT_MENTAL_ID, defaultMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalActivityResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("httpRefs", "user", "mentalTypeId")
                .isEqualTo(defaultMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("mental_activity", "httpRefTypeName")
                .isEqualTo(defaultMental1.getHttpRefsSortedById());
    }

    @Test
    void getDefaultMentalActivityById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentDefaultMentalActivityId = 1000L;
        ApiException expectedException = new ApiException(
                ErrorMessage.MENTAL_NOT_FOUND, nonExistentDefaultMentalActivityId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.DEFAULT_MENTAL_ID, nonExistentDefaultMentalActivityId)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalActivityById_shouldReturnDtoWith200_whenValidRequest() throws Exception {
        // Given
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(2);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalType mentalType2 = dbUtil.createMeditationType();

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);

        User user = dbUtil.createUser(1);
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(3, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(4, user);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1, customHttpRef1), mentalType1, user);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(4, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user);

        // When
        MvcResult mvcResult = mockMvc.perform(
                        get(URL.CUSTOM_MENTAL_ID, customMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String responseContent = mvcResult.getResponse().getContentAsString();
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, new TypeReference<MentalActivityResponseDto>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(customMental1);

        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users", "mental_activity", "httpRefTypeName")
                .isEqualTo(customMental1.getHttpRefsSortedById());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalActivityById_shouldReturnErrorMessageWith404_whenNotFound() throws Exception {
        // Given
        long nonExistentCustomMentalActivityId = 1000L;
        ApiException expectedException = new ApiException(
                ErrorMessage.MENTAL_NOT_FOUND, nonExistentCustomMentalActivityId, HttpStatus.NOT_FOUND);

        // When
        mockMvc.perform(get(URL.CUSTOM_MENTAL_ID, nonExistentCustomMentalActivityId)
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is(expectedException.getMessageWithResourceId())))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getCustomMentalActivityById_shouldReturnErrorMessageWith400_whenMentalActivityUserMismatch() throws Exception {
        // Given
        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        MentalActivity customMental =
                dbUtil.createCustomMentalActivity(3, List.of(defaultHttpRef1), mentalType1, user2);
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
    void getAllMentalActivities_shouldReturnListDefaultMentalActivitiesWith200_whenValidRequest() throws Exception {
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

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);
        MentalActivity defaultMental3 = dbUtil.createDefaultMentalActivity(3, List.of(defaultHttpRef1), mentalType2);
        MentalActivity defaultMental4 = dbUtil.createDefaultMentalActivity(4, List.of(defaultHttpRef2), mentalType1);
        MentalActivity customMental = dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1), mentalType1, user);

        List<MentalActivity> expectedMentalList = Stream.of(
                        defaultMental1, defaultMental2, defaultMental3, defaultMental4)
                .sorted(Comparator.comparingLong(MentalActivity::getId))
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
        List<MentalActivityResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalActivityResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getAllMentalActivities_shouldReturnListMentalActivitiesDtoWith200_whenValidRequest() throws Exception {
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

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType2);
        MentalActivity defaultMental3 = dbUtil.createDefaultMentalActivity(3, List.of(defaultHttpRef1), mentalType2);
        MentalActivity defaultMental4 = dbUtil.createDefaultMentalActivity(4, List.of(defaultHttpRef2), mentalType1);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(5, List.of(customHttpRef1), mentalType1, user1);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1, customHttpRef1), mentalType2, user1);
        MentalActivity customMental3 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef2, customHttpRef1), mentalType2, user1);
        MentalActivity customMental4 =
                dbUtil.createCustomMentalActivity(8, List.of(customHttpRef2), mentalType1, user2);
        MentalActivity customMental5 =
                dbUtil.createCustomMentalActivity(9, List.of(defaultHttpRef2, customHttpRef2), mentalType2, user2);

        List<MentalActivity> expectedMentalList = Stream.of(
                        defaultMental1,
                        defaultMental2,
                        defaultMental3,
                        defaultMental4,
                        customMental1,
                        customMental2,
                        customMental3)
                .sorted(Comparator.comparingLong(MentalActivity::getId))
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
        List<MentalActivityResponseDto> responseDto =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalActivityResponseDto>>() {});

        assertThat(responseDto)
                .usingRecursiveComparison()
                .ignoringFields("user", "httpRefs", "mentalTypeId")
                .isEqualTo(expectedMentalList);
    }

    @ParameterizedTest
    @MethodSource("updateCustomMentalActivityValidFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMentalActivity_shouldReturnUpdatedDtoWith200_whenValidFilters(
            String updateTitle, String updateDescription, String updatedMentalType) throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        MentalActivity customMental = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        HttpRef customHttpRefToAdd = dbUtil.createCustomHttpRef(5, user);
        HttpRef defaultHttpRefToAdd = dbUtil.createDefaultHttpRef(6);

        Optional<Long> mentalType = Stream.of(mentalType1, mentalType2)
                .filter(mentalTypes -> mentalTypes.getName().equals(updatedMentalType))
                .map(MentalType::getId)
                .findFirst();
        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDto(
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
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, MentalActivityResponseDto.class);

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

    static Stream<Arguments> updateCustomMentalActivityValidFilters() {
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
    @MethodSource("updateCustomMentalActivityInvalidFilters")
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

        MentalActivity customMental = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        Optional<Long> mentalType = Stream.of(mentalType1, mentalType2)
                .filter(mentalTypes -> mentalTypes.getName().equals(updatedMentalType))
                .map(MentalType::getId)
                .findFirst();

        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDtoEmpty();
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

    static Stream<Arguments> updateCustomMentalActivityInvalidFilters() {
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
    void updateCustomMentalActivity_shouldReturnUpdatedDtoWith200_whenHttpRefsIdsNotGiven() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        MentalActivity customMental = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        // Http refs should be removed from the target exercise. Other fields should remain the same.
        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDtoEmpty();
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
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, MentalActivityResponseDto.class);

        assertTrue(responseDto.isCustom());
        assertEquals(customMental.getTitle(), responseDto.getTitle());
        assertEquals(customMental.getDescription(), responseDto.getDescription());
        assertEquals(customMental.getType().getId(), responseDto.getMentalTypeId());
        assertThat(responseDto.getHttpRefs()).usingRecursiveComparison().isEqualTo(Collections.emptyList());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void updateCustomMentalActivity_shouldReturnErrorMessageWith404_whenCustomMentalActivityNotFound()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef = dbUtil.createDefaultHttpRef(1);
        long nonExistentMentalId = 1000L;

        MentalActivityUpdateRequestDto requestDto =
                dtoUtil.mentalActivityUpdateRequestDto(2, List.of(defaultHttpRef.getId()), mentalType2.getId());

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
    void updateCustomMentalActivity_shouldReturnErrorMessageWith400_whenEmptyRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        MentalActivity customMental = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDtoEmpty();
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
    void updateCustomMentalActivity_shouldReturnErrorMessageWith400_whenMentalActivityUserMismatch() throws Exception {
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

        MentalActivity customMental1 = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);

        User user2 = dbUtil.createUser(2, role, country);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(2, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user2);

        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDtoEmpty();
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
    void updateCustomMentalActivity_shouldReturnErrorMessageWith400_whenMentalActivityWithNewTitleAlreadyExists()
            throws Exception {
        // Given
        User user = dbUtil.createUser(1);

        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef customHttpRef2 = dbUtil.createCustomHttpRef(2, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(3);
        HttpRef defaultHttpRef2 = dbUtil.createDefaultHttpRef(4);

        MentalActivity customMental1 = dbUtil.createCustomMentalActivity(
                1, List.of(customHttpRef1, customHttpRef2, defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(2, List.of(customHttpRef1, defaultHttpRef1), mentalType2, user);

        MentalActivityUpdateRequestDto requestDto = dtoUtil.mentalActivityUpdateRequestDtoEmpty();

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
    void deleteCustomMentalActivity_shouldReturnVoidWith204_whenValidRequest() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(1, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user);

        long mentalIdToBeRemoved = customMental1.getId();

        // When
        mockMvc.perform(delete(URL.CUSTOM_MENTAL_ID, customMental1.getId()).contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist())
                .andDo(print());

        assertNull(dbUtil.getMentalActivityById(mentalIdToBeRemoved));
        assertTrue(dbUtil.httpRefsExistByIds(List.of(customHttpRef1.getId(), defaultHttpRef1.getId())));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void deleteCustomMentalActivity_shouldReturnErrorMessageWith404_whenMentalActivityNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(1, List.of(customHttpRef1, defaultHttpRef1), mentalType1, user);
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
    void createCustomMentalActivity_ReturnVoidWith204_whenValidRequest() throws Exception {
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        HttpRef customHttpRef1 = dbUtil.createCustomHttpRef(1, user);
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(2);
        MentalActivityCreateRequestDto customMentalRequestDto = dtoUtil.mentalActivityCreateRequestDto(
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
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, MentalActivityResponseDto.class);

        assertEquals(
                customMentalRequestDto.getHttpRefs().size(),
                responseDto.getHttpRefs().size());
        assertThat(responseDto.getHttpRefs())
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "exercises", "user", "mental_activity", "nutritions", "httpRefTypeName")
                .isEqualTo(List.of(customHttpRef1, defaultHttpRef1));

        // Db
        MentalActivity createdMental = dbUtil.getMentalActivityById(responseDto.getId());
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
                        "exercises", "user", "mentalActivities", "nutritions", "httpRefType")
                .isEqualTo(List.of(customHttpRef1, defaultHttpRef1));
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalActivity_ReturnVoidWith201_whenValidMandatoryFields() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        MentalActivityCreateRequestDto customMentalRequestDto =
                dtoUtil.mentalActivityCreateRequestDto(1, Collections.emptyList(), mentalType1.getId());
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
        MentalActivityResponseDto responseDto =
                objectMapper.readValue(responseContent, MentalActivityResponseDto.class);

        assertTrue(customMentalRequestDto.getHttpRefs().isEmpty());
        assertEquals(customMentalRequestDto.getMentalTypeId(), responseDto.getMentalTypeId());
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void createCustomMentalActivity_shouldReturnErrorMessageWith400_whenHttpRefNotFound() throws Exception {
        // Given
        User user = dbUtil.createUser(1);
        MentalType mentalType1 = dbUtil.createAffirmationType();
        long nonExistentHttpRefId = 1000L;
        MentalActivityCreateRequestDto mentalCreateRequestDto = dtoUtil.mentalActivityCreateRequestDto(
                1, Collections.singletonList(nonExistentHttpRefId), mentalType1.getId());
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
    @MethodSource("getMentalActivitiesWithFilterValidDefaultFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalActivitiesWithFilter_shouldReturnDefaultFilteredPageWith200_whenValidFilters(
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

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef2), mentalType1);
        MentalActivity defaultMental3 = dbUtil.createDefaultMentalActivity(3, List.of(defaultHttpRef3), mentalType2);
        MentalActivity defaultMental4 =
                dbUtil.createDefaultMentalActivity(4, List.of(defaultHttpRef4, defaultHttpRef3), mentalType2);

        User user = dbUtil.createUser(1);

        MentalActivity customMental1 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1, defaultHttpRef2), mentalType1, user);
        MentalActivity customMental2 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef3, defaultHttpRef2), mentalType1, user);

        List<MentalActivity> expectedFilteredMentalActivities = Stream.of(
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
        List<MentalActivityResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalActivityResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        Assertions.assertEquals(mentalResponseDtoList.size(), resultSeeds.size());
    }

    static Stream<Arguments> getMentalActivitiesWithFilterValidDefaultFilters() {
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
    @MethodSource("getMentalActivitiesWithFilterValidCustomFilters")
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalActivitiesWithFilter_shouldReturnCustomFilteredPageWith200_whenValidFilters(
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

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        MentalActivity customMental1User1 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2User1 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental1User2 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental2User2 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        List<MentalActivity> expectedFilteredMentalActivities = Stream.of(
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
        List<MentalActivityResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalActivityResponseDto>>() {});
        assertEquals(numberOfElementsCurrentPage, mentalResponseDtoList.size());
        assertEquals(expectedFilteredMentalActivities.size(), mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "httpRefs", "mentalTypeId", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentalActivities);
    }

    static Stream<Arguments> getMentalActivitiesWithFilterValidCustomFilters() {
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
    void getMentalActivitiesWithFilter_shouldReturnDefaultAndCustomFilteredPageWith200() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        MentalActivity customMental1User1 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2User1 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental1User2 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental2User2 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

        List<MentalActivity> expectedFilteredMentalActivities =
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
        List<MentalActivityResponseDto> mentalResponseDtoList =
                objectMapper.readValue(contentNode.toString(), new TypeReference<List<MentalActivityResponseDto>>() {});
        assertEquals(4, mentalResponseDtoList.size());
        assertThat(mentalResponseDtoList)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields(
                        "user", "httpRefs", "mentalTypeId", "nutrition", "exercise")
                .isEqualTo(expectedFilteredMentalActivities);
    }

    @Test
    @WithMockUser(username = "Username-1", password = "Password-1", roles = "USER")
    void getMentalActivitiesWithFilter_shouldReturnValidationErrorMessageWith400_whenInvalidFilters() throws Exception {
        // Given
        MentalType mentalType1 = dbUtil.createMeditationType();
        MentalType mentalType2 = dbUtil.createAffirmationType();
        HttpRef defaultHttpRef1 = dbUtil.createDefaultHttpRef(1);

        MentalActivity defaultMental1 = dbUtil.createDefaultMentalActivity(1, List.of(defaultHttpRef1), mentalType1);
        MentalActivity defaultMental2 = dbUtil.createDefaultMentalActivity(2, List.of(defaultHttpRef1), mentalType2);

        Role role = dbUtil.createUserRole();
        Country country = dbUtil.createCountry(1);
        User user1 = dbUtil.createUser(1, role, country);
        User user2 = dbUtil.createUser(2, role, country);

        MentalActivity customMental1User1 =
                dbUtil.createCustomMentalActivity(5, List.of(defaultHttpRef1), mentalType1, user1);
        MentalActivity customMental2User1 =
                dbUtil.createCustomMentalActivity(6, List.of(defaultHttpRef1), mentalType2, user1);
        MentalActivity customMental1User2 =
                dbUtil.createCustomMentalActivity(7, List.of(defaultHttpRef1), mentalType1, user2);
        MentalActivity customMental2User2 =
                dbUtil.createCustomMentalActivity(8, List.of(defaultHttpRef1), mentalType2, user2);

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
